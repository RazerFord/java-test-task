package ru.itmo.mit.asyncserver;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadCallback implements CompletionHandler<Integer, AsyncHandler> {
    private static final Logger LOGGER = Logger.getLogger(ReadCallback.class.getName());
    public static final ReadCallback INSTANCE = new ReadCallback();

    private ReadCallback() {
    }

    @Override
    public void completed(Integer result, @NotNull AsyncHandler attachment) {
        try {
            if (attachment.checkRead()) attachment.handleRead();
            else attachment.asyncRead();
        } catch (ShutdownChannelGroupException | InvalidProtocolBufferException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void failed(@NotNull Throwable t, AsyncHandler attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
