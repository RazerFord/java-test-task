package ru.itmo.mit.asyncserver;

import ru.itmo.mit.Pair;
import ru.itmo.mit.Server;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.StatisticsRecorder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.ShutdownChannelGroupException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
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
    private final Queue<AsyncHandler> asyncHandlers = new ConcurrentLinkedQueue<>();
    private final Lock acceptLock = new ReentrantLock();
    private final Lock bindLock = new ReentrantLock();
    private final Condition acceptCond = acceptLock.newCondition();
    private final Condition bindCond = bindLock.newCondition();
    private final InetSocketAddress inetSocketAddress;
    private final int backlog;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private boolean closed;
    private int realPort = -1;

    public AsyncServer(int serverPort, int backlog, StatisticsRecorder statisticsRecorder) {
        this(serverPort, backlog, NUMBER_THREADS, statisticsRecorder);
    }

    public AsyncServer(int serverPort, int backlog, int numberThreads, StatisticsRecorder statisticsRecorder) {
        this.inetSocketAddress = new InetSocketAddress(serverPort);
        this.backlog = backlog;
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
            serverChannel.bind(inetSocketAddress, backlog);
            updatePort(serverChannel.getLocalAddress());

            var asyncHandler = new AsyncHandler(threadPool, serverChannel, this, statisticsRecorder);
            asyncHandlers.add(asyncHandler);
            var attachment = new Pair<>(asyncHandlers, asyncHandler);
            serverChannel.accept(attachment, AcceptCallback.INSTANCE);

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
            while (realPort == -1) {
                bindCond.await();
            }
        } finally {
            bindLock.unlock();
        }
        return realPort;
    }

    @Override
    public void reset() {
        asyncHandlers.clear();
    }

    @Override
    public int getRequestProcessingTime() {
        Queue<Long> queue = new ArrayDeque<>();
        asyncHandlers.forEach(it -> it.addIfNotZeroRequestProcessingTime(queue));
        return statisticsRecorder.average(queue);
    }

    @Override
    public int getClientProcessingTime() {
        Queue<Long> queue = new ArrayDeque<>();
        asyncHandlers.forEach(it -> it.addIfNotZeroClientProcessingTime(queue));
        return statisticsRecorder.average(queue);
    }

    private void updatePort(SocketAddress socketAddress) {
        if (socketAddress instanceof InetSocketAddress address) {
            bindLock.lock();
            try {
                realPort = address.getPort();
                bindCond.signalAll();
            } finally {
                bindLock.unlock();
            }
        } else {
            throw new ServerException();
        }
    }
}
