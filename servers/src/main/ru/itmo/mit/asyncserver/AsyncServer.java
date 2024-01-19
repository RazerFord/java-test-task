package ru.itmo.mit.asyncserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(AsyncServer.class.getName());
    private static final int NUMBER_THREADS = Runtime.getRuntime().availableProcessors();
    private final Lock acceptLock = new ReentrantLock();
    private final Condition acceptCond = acceptLock.newCondition();
    private final InetSocketAddress inetSocketAddress;
    private final int numberThreads;
    private boolean closed;

    public AsyncServer(int serverPort) {
        this(serverPort, NUMBER_THREADS);
    }

    public AsyncServer(int serverPort, int numberThreads) {
        this.inetSocketAddress = new InetSocketAddress(serverPort);
        this.numberThreads = numberThreads;
    }

    @Override
    public void run() {
        try (var threadPool = Executors.newFixedThreadPool(numberThreads)) {
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(threadPool);
            run(threadPool, channelGroup);
            if (channelGroup != null) {
                channelGroup.shutdownNow();
            }
        } catch (IOException | ShutdownChannelGroupException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void run(ExecutorService threadPool, AsynchronousChannelGroup channelGroup) throws IOException {
        try (var serverChannel = AsynchronousServerSocketChannel.open(channelGroup)) {
            serverChannel.bind(inetSocketAddress);

            serverChannel.accept(new AsyncHandler(threadPool, serverChannel, this), AcceptCallback.INSTANCE);

            acceptLock.lock();
            while (!closed && !Thread.currentThread().isInterrupted()) {
                acceptCond.await();
            }
            acceptLock.unlock();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void close() {
        acceptLock.lock();
        closed = true;
        acceptCond.signal();
        acceptLock.unlock();
    }
}
