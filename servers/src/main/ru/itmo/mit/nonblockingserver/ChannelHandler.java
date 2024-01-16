package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.Utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

public class ChannelHandler {
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private static final String ERR_MSG = "SelectionKey must not be null";
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private final Queue<ByteBuffer> writeBuffers = new LinkedBlockingQueue<>();
    private final SocketChannel socketChannel;
    private final ExecutorService threadPool;
    private final SelectorWriter selectorWriter;
    private SelectionKey selectionKeyRead;
    private SelectionKey selectionKeyWrite;

    public ChannelHandler(SocketChannel socketChannel, ExecutorService threadPool, SelectorWriter selectorWriter) {
        this.socketChannel = socketChannel;
        this.threadPool = threadPool;
        this.selectorWriter = selectorWriter;
    }

    public void tryRead() throws IOException {
        if (!readBuffer.hasRemaining() || sizeMessage > readBuffer.position()) {
            increaseReadBufferAfterCompact();
        }

        try {
            if (socketChannel.read(readBuffer) == -1) return;
        } catch (IOException e) {
            threadPool.execute(this::handleClosing);
            throw new ServerException(e);
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
            threadPool.execute(() -> handle(message.getNumberList()));
            readBuffer.position(sizeMessage);
            readBuffer.compact();
            sizeMessage = -1;
        }
    }

    public void tryWrite() throws IOException {
        var head = writeBuffers.peek();
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
        try {
            socketChannel.write(head);
            if (!head.hasRemaining()) {
                writeBuffers.poll();
            }
        } catch (IOException e) {
            writeBuffers.clear();
            throw new ServerException(e);
        } finally {
            if (writeBuffers.isEmpty()) {
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
        return sizeMessage <= readBuffer.position();
    }

    private void handle(List<Integer> numbers) {
        var numbers1 = new ArrayList<>(numbers);
        Collections.sort(numbers1);
        MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
        final int size = message.getSerializedSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
        byteBuffer.putInt(size).put(message.toByteArray());
        byteBuffer.flip();
        writeBuffers.add(byteBuffer);
        selectorWriter.addAndWakeup(this);
    }

    private void handleClosing() {
        Objects.requireNonNull(selectionKeyRead, ERR_MSG).cancel();
        selectionKeyRead = null;
        Utils.run(socketChannel::close);
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }
}
