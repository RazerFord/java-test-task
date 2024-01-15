package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class ChannelHandler {
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private static final int INITIAL_WRITE_BUFFER_SIZE = 1024;
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(INITIAL_WRITE_BUFFER_SIZE);
    private final SocketChannel socketChannel;

    public ChannelHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void tryRead() throws IOException {
        if (socketChannel.read(readBuffer) == -1) return;
        int readBytes = readBuffer.position();
        if (sizeMessage == -1) {
            if (readBytes < Integer.BYTES) return;
            sizeMessage = readBuffer.flip().getInt();
            readBuffer.compact();
            if (sizeMessage > readBuffer.position()) increaseReadBuffer();
        }
        if (messageReady()) {
            var message = MessageOuterClass.Message.parseFrom(readBuffer);
            readBuffer.compact();
            sizeMessage = -1;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public SelectionKey register(@NotNull Selector selector, int ops) throws ClosedChannelException {
        return socketChannel.register(selector, ops, this);
    }

    private boolean messageReady() {
        return sizeMessage == readBuffer.position();
    }

    private void increaseReadBuffer() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        newByteBuffer.limit(readBuffer.limit());
        readBuffer = newByteBuffer;
    }
}
