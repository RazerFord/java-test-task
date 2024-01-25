package ru.itmo.mit.nonblockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Server;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.StatisticsRecorder;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import static ru.itmo.mit.Utils.queueToQueueLong;

public class NonBlockingServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(NonBlockingServer.class.getName());
    private static final int MIN_NUMBER_THREADS = 4;
    private static final int NUMBER_THREADS_TO_SELECTORS = 2;
    private static final int NUMBER_THREADS = Integer.max(Runtime.getRuntime().availableProcessors() - NUMBER_THREADS_TO_SELECTORS, MIN_NUMBER_THREADS);
    private final Lock bindLock = new ReentrantLock();
    private final Condition bindCond = bindLock.newCondition();
    private final Queue<ChannelHandler> channelHandlers = new ArrayDeque<>();
    private final SocketAddress inetAddress;
    private final int backlog;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private ServerSocketChannel socketChannel;
    private boolean closed;
    private int realPort = -1;

    public NonBlockingServer(int serverPort, int backlog, StatisticsRecorder statisticsRecorder) {
        this(serverPort, backlog, NUMBER_THREADS, statisticsRecorder);
    }

    public NonBlockingServer(int serverPort, int backlog, int numberThreads, StatisticsRecorder statisticsRecorder) {
        inetAddress = new InetSocketAddress(serverPort);
        this.backlog = backlog;
        this.numberThreads = numberThreads;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void run() {
        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.socket().bind(inetAddress, backlog);
            run(socketChannel);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    public void run(@NotNull ServerSocketChannel socketChannel) throws IOException {
        updatePort(socketChannel.getLocalAddress());
        try (
                var socketChannel1 = socketChannel;
                var readSelector = Selector.open();
                var writeSelector = Selector.open();
                var threadPool = Executors.newFixedThreadPool(numberThreads)
        ) {
            SelectorReader selectorReader = new SelectorReader(readSelector);
            Thread threadSelectorReader = new Thread(selectorReader);
            threadSelectorReader.start();
            SelectorWriter selectorWriter = new SelectorWriter(writeSelector);
            Thread threadSelectorWriter = new Thread(selectorWriter);
            threadSelectorWriter.start();

            while (!closed && socketChannel1.isOpen() && !Thread.currentThread().isInterrupted()) {
                SocketChannel clientSocketChannel = socketChannel1.accept();
                clientSocketChannel.configureBlocking(false);
                var channelHandler = new ChannelHandler(clientSocketChannel, threadPool, selectorWriter, statisticsRecorder);
                channelHandlers.add(channelHandler);
                selectorReader.addAndWakeup(channelHandler);
            }
        } catch (SocketException | AsynchronousCloseException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (socketChannel != null) socketChannel.close();
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
        channelHandlers.clear();
    }

    @Override
    public int getRequestProcessingTime() {
        return statisticsRecorder.average(queueToQueueLong(channelHandlers, ChannelHandler::getRequestProcessingTime));
    }

    @Override
    public int getClientProcessingTime() {
        return statisticsRecorder.average(queueToQueueLong(channelHandlers, ChannelHandler::getClientProcessingTime));
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
