package ru.itmo.mit.blockingserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BlockingServer implements Server, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(BlockingServer.class.getName());
    private static final int NUMBER_THREADS = 10;
    private final int serverPort;
    private final int numberThreads;
    private ServerSocket socket;
    private boolean closed;

    public BlockingServer(int serverPort) {
        this(serverPort, NUMBER_THREADS);
    }

    public BlockingServer(int serverPort, int numberThreads) {
        this.serverPort = serverPort;
        this.numberThreads = numberThreads;
    }

    @Override
    public void start() throws IOException {
        socket = new ServerSocket(serverPort);
        try (var socket1 = socket;
             var threadPool = Executors.newFixedThreadPool(numberThreads)) {
            while (!closed && !socket1.isClosed() && !Thread.currentThread().isInterrupted()) {
                var port = socket1.accept();
                Thread thread = new Thread(new Handler(port, threadPool));
                thread.setDaemon(true);
                thread.start();
            }
        } catch (SocketException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        closed = true;
        if (socket != null && !socket.isClosed()) socket.close();
    }
}
