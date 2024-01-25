package ru.itmo.mit.blockingserver;

import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockingServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(BlockingServer.class.getName());
    private static final int NUMBER_THREADS = Runtime.getRuntime().availableProcessors();
    private final Lock bindLock = new ReentrantLock();
    private final Condition bindCond = bindLock.newCondition();
    private final Queue<Handler> handlers = new ConcurrentLinkedQueue<>();
    private final int serverPort;
    private final int backlog;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private ServerSocket socket;
    private boolean closed;

    public BlockingServer(int serverPort, int backlog, StatisticsRecorder statisticsRecorder) {
        this(serverPort, backlog, NUMBER_THREADS, statisticsRecorder);
    }

    public BlockingServer(int serverPort, int backlog, int numberThreads, StatisticsRecorder statisticsRecorder) {
        this.serverPort = serverPort;
        this.backlog = backlog;
        this.numberThreads = numberThreads;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(serverPort, backlog);
            run(socket);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void run(ServerSocket socket) throws IOException {
        bindLock.lock();
        try {
            bindCond.signalAll();
        } finally {
            bindLock.unlock();
        }
        try (
                var socket1 = socket;
                var threadPool = Executors.newFixedThreadPool(numberThreads)
        ) {
            while (!closed && !socket1.isClosed() && !Thread.currentThread().isInterrupted()) {
                var port = socket1.accept();
                var handler = new Handler(port, threadPool, statisticsRecorder);
                handlers.add(handler);
                Thread thread = new Thread(handler);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void close() throws IOException {
        closed = true;
        if (socket != null && !socket.isClosed()) socket.close();
    }

    @Override
    public int getPort() throws InterruptedException {
        bindLock.lock();
        try {
            while (socket == null || socket.getLocalPort() == -1) {
                bindCond.await();
            }
        } finally {
            bindLock.unlock();
        }
        return socket.getLocalPort();
    }

    @Override
    public void reset() {
        handlers.clear();
    }

    @Override
    public long getRequestProcessingTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getClientProcessingTime() {
        throw new UnsupportedOperationException();
    }
}
