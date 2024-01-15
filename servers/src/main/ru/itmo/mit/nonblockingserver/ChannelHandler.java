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
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private Queue<ByteBuffer> writeBuffers = new LinkedBlockingQueue<>();
    ;
    private final SocketChannel socketChannel;
    private final ExecutorService threadPool;

    public ChannelHandler(SocketChannel socketChannel, ExecutorService threadPool) {
        this.socketChannel = socketChannel;
        this.threadPool = threadPool;
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

    @SuppressWarnings("UnusedReturnValue")
    public SelectionKey register(@NotNull Selector selector, int ops) throws ClosedChannelException {
        return socketChannel.register(selector, ops, this);
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
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }
}
