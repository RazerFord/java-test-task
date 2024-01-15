package ru.itmo.mit.blockingserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;

public class BlockingServer implements Server {
    private static final int NUMBER_THREADS = 10;
    private final int serverPort;
    private final int numberThreads;

    public BlockingServer(int serverPort) {
        this(serverPort, NUMBER_THREADS);
    }

    public BlockingServer(int serverPort, int numberThreads) {
        this.serverPort = serverPort;
        this.numberThreads = numberThreads;
    }

    @Override
    public void start() throws IOException {
        try (var socket = new ServerSocket(serverPort);
             var threadPool = Executors.newFixedThreadPool(numberThreads)) {
            while (!socket.isClosed() && !Thread.currentThread().isInterrupted()) {
                var port = socket.accept();
                Thread thread = new Thread(new Handler(port, threadPool));
                thread.setDaemon(true);
                thread.start();
            }
        }
    }
}
