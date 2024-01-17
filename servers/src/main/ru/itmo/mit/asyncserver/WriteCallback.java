package ru.itmo.mit.asyncserver;

import org.jetbrains.annotations.NotNull;

import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WriteCallback implements CompletionHandler<Integer, AsyncHandler> {
    private static final Logger LOGGER = Logger.getLogger(WriteCallback.class.getName());
    public static final WriteCallback INSTANCE = new WriteCallback();

    private WriteCallback() {
    }

    @Override
    public void completed(Integer result, @NotNull AsyncHandler attachment) {
        try {
            if (attachment.checkWrite()) attachment.handleWrite();
            else attachment.asyncWrite();
        } catch (ShutdownChannelGroupException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void failed(@NotNull Throwable t, AsyncHandler attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
