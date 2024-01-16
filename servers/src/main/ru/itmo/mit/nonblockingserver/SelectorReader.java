package ru.itmo.mit.nonblockingserver;

import java.io.IOException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SelectorReader implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(SelectorReader.class.getName());
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
                    channelHandler.registerRead(selector);
                }
            }
        } catch (IOException | ClosedSelectorException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    public void addAndWakeup(ChannelHandler channelHandler) {
        channelHandlerQueue.add(channelHandler);
        selector.wakeup();
    }
}
