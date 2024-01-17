package ru.itmo.mit.asyncserver;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.itmo.mit.MessageOuterClass;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AsyncHandler {
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private final ExecutorService executorService;
    private final AsynchronousServerSocketChannel asyncServerSocketChannel;
    private final AsyncServer asyncServer;
    private AsynchronousSocketChannel asyncSocketChannel;

    public AsyncHandler(
            ExecutorService executorService,
            AsynchronousServerSocketChannel asyncServerSocketChannel,
            AsyncServer asyncServer
    ) {
        this.executorService = executorService;
        this.asyncServerSocketChannel = asyncServerSocketChannel;
        this.asyncServer = asyncServer;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public AsyncServer getAsyncServer() {
        return asyncServer;
    }

    public AsynchronousServerSocketChannel getAsyncServerSocketChannel() {
        return asyncServerSocketChannel;
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    public void setAsyncSocketChannel(AsynchronousSocketChannel asyncSocketChannel) {
        this.asyncSocketChannel = asyncSocketChannel;
    }

    public void asyncRead() {
        asyncSocketChannel.read(readBuffer, this, new ReadCallback());
    }

    public void asyncWrite() throws InvalidProtocolBufferException {
        readBuffer.flip();
        var message = MessageOuterClass.Message.parseFrom(readBuffer);
        readBuffer.position(sizeMessage);
        readBuffer.compact();
        sizeMessage = -1;
        executorService.execute(() -> handle(message.getNumberList()));
    }

    public Status check() {
        int readBytes = readBuffer.position();
        if (!readBuffer.hasRemaining() || sizeMessage > readBytes) {
            increaseReadBufferAfterCompact();
        }

        if (sizeMessage == -1) {
            if (readBytes < Integer.BYTES) return Status.READ;
            sizeMessage = readBuffer.flip().getInt();
            readBuffer.compact();
        }

        if (messageReady()) {
            return Status.WRITE;
        }
        return Status.READ;
    }

    public AsyncHandler copy() {
        return new AsyncHandler(executorService, asyncServerSocketChannel, asyncServer);
    }

    private void handle(List<Integer> numbers) {
        var numbers1 = new ArrayList<>(numbers);
        Collections.sort(numbers1);
        MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
        final int size = message.getSerializedSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
        byteBuffer.putInt(size).put(message.toByteArray());
        byteBuffer.flip();
        asyncSocketChannel.write(byteBuffer, this, new WriteCallback());
        asyncRead();
    }

    private boolean messageReady() {
        return sizeMessage != -1 && sizeMessage <= readBuffer.position();
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }

    public enum Status {
        READ,
        WRITE,
    }
}
