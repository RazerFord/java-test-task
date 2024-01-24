package ru.itmo.mit.asyncserver;

import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcceptCallback implements CompletionHandler<AsynchronousSocketChannel, AsyncHandler> {
    private static final Logger LOGGER = Logger.getLogger(AcceptCallback.class.getName());
    public static final AcceptCallback INSTANCE = new AcceptCallback();
    private AcceptCallback(){}

    @Override
    public void completed(@NotNull AsynchronousSocketChannel result, @NotNull AsyncHandler attachment) {
        try {
            var asyncServerSocketChannel = attachment.getAsyncServerSocketChannel();
            if (asyncServerSocketChannel.isOpen()) {
                asyncServerSocketChannel.accept(attachment.copy(), INSTANCE);
            }
            attachment.setAsyncSocketChannel(result);
            attachment.asyncRead();
        } catch (ShutdownChannelGroupException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void failed(@NotNull Throwable t, AsyncHandler attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
