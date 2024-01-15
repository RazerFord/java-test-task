package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class ChannelHandler {
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private static final int INITIAL_WRITE_BUFFER_SIZE = 1024;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(INITIAL_WRITE_BUFFER_SIZE);
    private final SocketChannel socketChannel;

    public ChannelHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @SuppressWarnings("UnusedReturnValue")
    public SelectionKey register(@NotNull Selector selector, int ops) throws ClosedChannelException {
        return socketChannel.register(selector, ops, this);
    }
}
