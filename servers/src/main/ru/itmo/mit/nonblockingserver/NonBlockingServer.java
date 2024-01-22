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
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NonBlockingServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(NonBlockingServer.class.getName());
    private static final int NUMBER_THREADS = 10;
    private final Lock bindLock = new ReentrantLock();
    private final Condition bindCond = bindLock.newCondition();
    private final SocketAddress inetAddress;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private ServerSocketChannel socketChannel;
    private boolean closed;
    private int realPort = -1;

    public NonBlockingServer(int serverPort, StatisticsRecorder statisticsRecorder) {
        this(serverPort, NUMBER_THREADS, statisticsRecorder);
    }

    public NonBlockingServer(int serverPort, int numberThreads, StatisticsRecorder statisticsRecorder) {
        inetAddress = new InetSocketAddress(serverPort);
        this.numberThreads = numberThreads;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void run() {
        try {
            socketChannel = ServerSocketChannel.open();
            socketChannel.socket().bind(inetAddress);
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
                selectorReader.addAndWakeup(new ChannelHandler(clientSocketChannel, threadPool, selectorWriter, statisticsRecorder));
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
