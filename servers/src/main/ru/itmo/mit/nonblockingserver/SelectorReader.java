package ru.itmo.mit.nonblockingserver;

import java.nio.channels.SocketChannel;

public class SelectorReader implements Runnable {
    private final SocketChannel socketChannel;

    public SelectorReader(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {

    }
}
