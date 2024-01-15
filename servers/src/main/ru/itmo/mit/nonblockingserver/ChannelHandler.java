package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;

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
    private SelectionKey selectionKey;

    public ChannelHandler(SocketChannel socketChannel, ExecutorService threadPool, SelectorWriter selectorWriter) {
        this.socketChannel = socketChannel;
        this.threadPool = threadPool;
        this.selectorWriter = selectorWriter;
    }

    public void tryRead() throws IOException {
        if (!readBuffer.hasRemaining() || sizeMessage > readBuffer.position()) {
            increaseReadBufferAfterCompact();
        }

        if (socketChannel.read(readBuffer) == -1) return;
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
            readBuffer.compact();
            sizeMessage = -1;
        }
    }

    public void tryWrite() throws IOException {
        var head = writeBuffers.peek();
        if (head == null) return;
        socketChannel.write(head);
        if (!head.hasRemaining()) {
            writeBuffers.poll();
        }
        if (writeBuffers.isEmpty()) {
            Objects.requireNonNull(selectionKey, ERR_MSG).cancel();
        }
    }

    public void register(@NotNull Selector selector, int ops) throws ClosedChannelException {
        selectionKey = socketChannel.register(selector, ops, this);
    }

    private boolean messageReady() {
        return sizeMessage <= readBuffer.position();
    }

    private void handle(List<Integer> numbers) {
        var numbers1 = new ArrayList<>(numbers);
        Collections.sort(numbers1);
        MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + message.getSerializedSize());
        byteBuffer.putInt(numbers1.size()).put(message.toByteArray());
        writeBuffers.add(byteBuffer);
        selectorWriter.addAndWakeup(this);
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }
}
