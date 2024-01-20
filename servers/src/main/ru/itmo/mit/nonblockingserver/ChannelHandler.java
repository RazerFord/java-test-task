package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.Pair;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChannelHandler {
    private static final Logger LOGGER = Logger.getLogger(ChannelHandler.class.getName());
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private static final String ERR_MSG = "SelectionKey must not be null";
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private final Queue<Pair<ByteBuffer, Runnable>> writeBuffersAndExecutors = new LinkedBlockingQueue<>();
    private final SocketChannel socketChannel;
    private final ExecutorService threadPool;
    private final SelectorWriter selectorWriter;
    private final StatisticsRecorder statisticsRecorder;
    private SelectionKey selectionKeyRead;
    private SelectionKey selectionKeyWrite;

    public ChannelHandler(
            SocketChannel socketChannel,
            ExecutorService threadPool,
            SelectorWriter selectorWriter,
            StatisticsRecorder statisticsRecorder
    ) {
        this.socketChannel = socketChannel;
        this.threadPool = threadPool;
        this.selectorWriter = selectorWriter;
        this.statisticsRecorder = statisticsRecorder;
    }

    public void tryRead() throws IOException {
        if (!readBuffer.hasRemaining() || sizeMessage > readBuffer.position()) {
            increaseReadBufferAfterCompact();
        }

        try {
            if (socketChannel.read(readBuffer) == -1) return;
        } catch (IOException e) {
            threadPool.execute(this::handleClosing);
            LOGGER.log(Level.WARNING, e.getMessage());
            return;
        }
        int readBytes = readBuffer.position();

        if (sizeMessage == -1) {
            if (readBytes < Integer.BYTES) return;
            sizeMessage = readBuffer.flip().getInt();
            readBuffer.compact();
        }

        if (messageReady()) {
            readBuffer.flip();
            var message = MessageOuterClass.Message.parseFrom(readBuffer);
            var start = Instant.now();
            threadPool.execute(() -> handle(message.getNumberList(), Utils.createActionAfterCompletion(statisticsRecorder, start)));
            readBuffer.position(sizeMessage);
            readBuffer.compact();
            sizeMessage = -1;
        }
    }

    public void tryWrite() throws IOException {
        var head = writeBuffersAndExecutors.peek();
        // Может быть `null` если два потока добавили буферы
        // в очередь на запись. Один из них добавил себя в
        // очередь на регистрацию к селектору на запись, и вызвал `wakeup`.
        // Селектор проснулся, зарегистрировался и отправил
        // данные из обоих буферов, разрегистрировался, уснул.
        // Затем вызывается регистрация с последующим wakeup из второго потока.
        // Но данных в буфере уже нет
        if (head == null) {
            Objects.requireNonNull(selectionKeyWrite, ERR_MSG).cancel();
            selectionKeyWrite = null;
            return;
        }
        var byteBuffer = head.first();
        head.second().run();
        try {
            socketChannel.write(byteBuffer);
            if (!byteBuffer.hasRemaining()) {
                writeBuffersAndExecutors.poll();
            }
        } catch (IOException e) {
            writeBuffersAndExecutors.clear();
            LOGGER.log(Level.WARNING, e.getMessage());
        } finally {
            if (writeBuffersAndExecutors.isEmpty()) {
                Objects.requireNonNull(selectionKeyWrite, ERR_MSG).cancel();
                selectionKeyWrite = null;
            }
        }
    }

    public void registerRead(@NotNull Selector selector) throws ClosedChannelException {
        selectionKeyRead = socketChannel.register(selector, SelectionKey.OP_READ, this);
    }

    public void registerWrite(@NotNull Selector selector) throws ClosedChannelException {
        selectionKeyWrite = socketChannel.register(selector, SelectionKey.OP_WRITE, this);
    }

    private boolean messageReady() {
        return sizeMessage != -1 && sizeMessage <= readBuffer.position();
    }

    private void handle(List<Integer> numbers, Runnable actionAfterCompletion) {
        var numbers1 = new ArrayList<>(numbers);
        var start = Instant.now();
        Utils.bubbleSort(numbers1);
        var end = Instant.now();
        statisticsRecorder.addRecord(Duration.between(start, end).toMillis(), StatisticsRecorder.SELECTOR_PROCESSING_REQUEST);
        MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
        final int size = message.getSerializedSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
        byteBuffer.putInt(size).put(message.toByteArray());
        byteBuffer.flip();
        writeBuffersAndExecutors.add(new Pair<>(byteBuffer, actionAfterCompletion));
        selectorWriter.addAndWakeup(this);
    }

    // Если делать в thread pool, то может быть ситуация,
    // что селектор будет пытаться прочитать из закрытого
    // канала
    private void handleClosing() {
        Objects.requireNonNull(selectionKeyRead, ERR_MSG).cancel();
        selectionKeyRead = null;
        Utils.run(socketChannel::close);
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.capacity();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }
}
