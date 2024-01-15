package ru.itmo.mit.nonblockingserver;

import ru.itmo.mit.ServerException;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorWriter implements Runnable {
    private final Selector selector;
    private final Queue<ChannelHandler> channelHandlerQueue = new LinkedBlockingQueue<>();

    public SelectorWriter(Selector selector) {
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
                    if (selectedKey.isWritable()) {
                        var channelHandler = (ChannelHandler) selectedKey.attachment();
                        channelHandler.tryWrite();
                    }
                    iterator.remove();
                }
                while (!channelHandlerQueue.isEmpty()) {
                    ChannelHandler channelHandler = channelHandlerQueue.poll();
                    channelHandler.register(selector, SelectionKey.OP_WRITE);
                }
            }
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    public boolean addAndWakeup(ChannelHandler channelHandler) {
        boolean status = channelHandlerQueue.add(channelHandler);
        selector.wakeup();
        return status;
    }

    public boolean add(ChannelHandler channelHandler) {
        return channelHandlerQueue.add(channelHandler);
    }

    public void wakeup() {
        selector.wakeup();
    }
}
