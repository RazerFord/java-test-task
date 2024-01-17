package ru.itmo.mit.asyncserver;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.itmo.mit.MessageOuterClass;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

public class AsyncHandler {
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private final ExecutorService executorService;
    private AsynchronousSocketChannel asyncSocketChannel;

    public AsyncHandler(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setAsyncSocketChannel(AsynchronousSocketChannel asyncSocketChannel) {
        this.asyncSocketChannel = asyncSocketChannel;
    }

    public void asyncRead() {
        asyncSocketChannel.read(readBuffer, this, new ReadCallback());
    }

    public void process() throws InvalidProtocolBufferException {
        if (!readBuffer.hasRemaining() || sizeMessage > readBuffer.position()) {
            increaseReadBufferAfterCompact();
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
//            threadPool.execute(() -> handle(message.getNumberList()));
            readBuffer.position(sizeMessage);
            readBuffer.compact();
            sizeMessage = -1;
        }
    }

    public ByteBuffer getReadBuffer() {
        return readBuffer;
    }

    private boolean messageReady() {
        return sizeMessage <= readBuffer.position();
    }

    private void increaseReadBufferAfterCompact() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }
}
