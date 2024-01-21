package ru.itmo.mit.asyncserver;

import ru.itmo.mit.Server;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.StatisticsRecorder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
    private final Lock bindLock = new ReentrantLock();
    private final Condition acceptCond = acceptLock.newCondition();
    private final Condition bindCond = bindLock.newCondition();
    private final InetSocketAddress inetSocketAddress;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private boolean closed;
    private int port = -1;

    public AsyncServer(int serverPort, StatisticsRecorder statisticsRecorder) {
        this(serverPort, NUMBER_THREADS, statisticsRecorder);
    }

    public AsyncServer(int serverPort, int numberThreads, StatisticsRecorder statisticsRecorder) {
        this.inetSocketAddress = new InetSocketAddress(serverPort);
        this.numberThreads = numberThreads;
        this.statisticsRecorder = statisticsRecorder;
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
            updatePort(serverChannel.getLocalAddress());

            serverChannel.accept(new AsyncHandler(threadPool, serverChannel, this, statisticsRecorder), AcceptCallback.INSTANCE);

            acceptLock.lock();
            try {
                while (!closed && !Thread.currentThread().isInterrupted()) {
                    acceptCond.await();
                }
            } finally {
                acceptLock.unlock();
            }
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

    @Override
    public int getPort() throws InterruptedException {
        bindLock.lock();
        try {
            while (port == -1) {
                bindCond.await();
            }
        } finally {
            bindLock.unlock();
        }
        return port;
    }

    private void updatePort(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress address) {
            bindLock.lock();
            try {
                port = address.getPort();
                bindCond.signalAll();
            } finally {
                bindLock.unlock();
            }
        } else {
            throw new ServerException();
        }
    }
}
