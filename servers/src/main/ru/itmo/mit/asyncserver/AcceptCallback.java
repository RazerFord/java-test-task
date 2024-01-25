package ru.itmo.mit.asyncserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Pair;

import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AcceptCallback implements CompletionHandler<AsynchronousSocketChannel, Pair<Queue<AsyncHandler>, AsyncHandler>> {
    private static final Logger LOGGER = Logger.getLogger(AcceptCallback.class.getName());
    public static final AcceptCallback INSTANCE = new AcceptCallback();

    private AcceptCallback() {
    }

    @Override
    public void completed(@NotNull AsynchronousSocketChannel result, @NotNull Pair<Queue<AsyncHandler>, AsyncHandler> attachment) {
        try {
            var asyncHandler = attachment.second();
            var asyncServerSocketChannel = asyncHandler.getAsyncServerSocketChannel();
            if (asyncServerSocketChannel.isOpen()) {
                var asyncHandlers = attachment.first();
                var newAsyncHandler = asyncHandler.copy();
                asyncHandlers.add(newAsyncHandler);
                attachment = new Pair<>(asyncHandlers, newAsyncHandler);
                asyncServerSocketChannel.accept(attachment, INSTANCE);
            }
            asyncHandler.setAsyncSocketChannel(result);
            asyncHandler.asyncRead();
        } catch (ShutdownChannelGroupException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void failed(@NotNull Throwable t, Pair<Queue<AsyncHandler>, AsyncHandler> attachment) {
        LOGGER.log(Level.WARNING, t.getMessage());
    }
}
