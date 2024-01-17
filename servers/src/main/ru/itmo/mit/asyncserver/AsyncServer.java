package ru.itmo.mit.asyncserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AsyncServer implements Server, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(AsyncServer.class.getName());
    private static final int NUMBER_THREADS = Runtime.getRuntime().availableProcessors();
    private final Lock acceptLock = new ReentrantLock();
    private final Condition acceptCond = acceptLock.newCondition();
    private final InetSocketAddress inetSocketAddress;
    private final int numberThreads;
    private AsynchronousServerSocketChannel serverChannel;
    private boolean closed;

    public AsyncServer(int serverPort) {
        this(serverPort, NUMBER_THREADS);
    }

    public AsyncServer(int serverPort, int numberThreads) {
        this.inetSocketAddress = new InetSocketAddress(serverPort);
        this.numberThreads = numberThreads;
    }

    @Override
    public void start() throws IOException {
        try (var threadPool = Executors.newFixedThreadPool(numberThreads)) {
            AsynchronousChannelGroup channelGroup = AsynchronousChannelGroup.withThreadPool(threadPool);

            try (var serverChannel1 = AsynchronousServerSocketChannel.open(channelGroup)) {
                serverChannel = serverChannel1;
                serverChannel1.bind(inetSocketAddress);

                serverChannel1.accept(new AsyncHandler(threadPool, serverChannel1, this), AcceptCallback.INSTANCE);

                acceptLock.lock();
                while (!closed && !Thread.currentThread().isInterrupted()) {
                    acceptCond.await();
                }
                acceptLock.unlock();
            } catch (InterruptedException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
            } finally {
                if (channelGroup != null) channelGroup.shutdown();
            }
        } catch (ShutdownChannelGroupException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        acceptLock.lock();
        closed = true;
        if (serverChannel != null) serverChannel.close();
        acceptCond.signal();
        acceptLock.unlock();
    }
}
