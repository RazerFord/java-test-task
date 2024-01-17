package ru.itmo.mit.asyncserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class AsyncServer implements Server, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(AsyncServer.class.getName());
    private static final int NUMBER_THREADS = Runtime.getRuntime().availableProcessors();
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

                serverChannel1.accept(new AsyncHandler(threadPool), new AcceptCallback());
            } finally {
                if (channelGroup != null) channelGroup.shutdown();
            }
        }
    }

    @Override
    public void close() throws Exception {
        closed = true;
        if (serverChannel != null) serverChannel.close();
    }
}
