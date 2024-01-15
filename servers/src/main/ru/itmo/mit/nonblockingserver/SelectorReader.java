package ru.itmo.mit.nonblockingserver;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorReader implements Runnable {
    private final Selector selector;
    private final Queue<ChannelHandler> channelHandlerQueue = new LinkedBlockingQueue<>();

    public SelectorReader(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            while (selector.isOpen()) {
                selector.select();
                var selectedKeys = selector.selectedKeys();
                var iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    var selectedKey = iterator.next();
                    if (selectedKey.isReadable()) {
                        var channelHandler = (ChannelHandler) selectedKey.attachment();
                        channelHandler.tryRead();
                    }
                    iterator.remove();
                }
                while (!channelHandlerQueue.isEmpty()) {
                    ChannelHandler channelHandler = channelHandlerQueue.poll();
                    channelHandler.register(selector, SelectionKey.OP_READ);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean add(ChannelHandler channelHandler) {
        return channelHandlerQueue.add(channelHandler);
    }
}
