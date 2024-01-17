package ru.itmo.mit.asyncserver;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jetbrains.annotations.NotNull;

import java.nio.channels.CompletionHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadCallback implements CompletionHandler<Integer, AsyncHandler> {
    private static final Logger LOGGER = Logger.getLogger(ReadCallback.class.getName());
    @Override
    public void completed(Integer result, @NotNull AsyncHandler attachment) {
        try {
            attachment.process();
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void failed(@NotNull Throwable t, AsyncHandler attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
