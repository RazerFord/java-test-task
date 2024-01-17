package ru.itmo.mit.asyncserver;

import org.jetbrains.annotations.NotNull;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcceptCallback implements CompletionHandler<AsynchronousSocketChannel, AsyncHandler> {
    private static final Logger LOGGER = Logger.getLogger(AcceptCallback.class.getName());

    @Override
    public void completed(@NotNull AsynchronousSocketChannel result, @NotNull AsyncHandler attachment) {
        attachment.setAsyncSocketChannel(result);
        attachment.asyncRead();
        System.out.println("SUCCESS");
    }

    @Override
    public void failed(@NotNull Throwable t, AsyncHandler attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
