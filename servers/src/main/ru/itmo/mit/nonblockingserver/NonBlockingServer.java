package ru.itmo.mit.nonblockingserver;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NonBlockingServer implements Server, AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(NonBlockingServer.class.getName());
    private static final int NUMBER_THREADS = 10;
    private final SocketAddress inetAddress;
    private final int numberThreads;
    private ServerSocketChannel socketChannel;
    private boolean closed;

    public NonBlockingServer(int serverPort) {
        this(serverPort, NUMBER_THREADS);
    }

    public NonBlockingServer(int serverPort, int numberThreads) {
        inetAddress = new InetSocketAddress(serverPort);
        this.numberThreads = numberThreads;
    }

    @Override
    public void start() throws IOException {
        socketChannel = ServerSocketChannel.open();
        socketChannel.socket().bind(inetAddress);

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
                selectorReader.addAndWakeup(new ChannelHandler(clientSocketChannel, threadPool, selectorWriter));
            }
        } catch (SocketException | AsynchronousCloseException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    @Override
    public void close() throws Exception {
        closed = true;
        if (socketChannel != null) socketChannel.close();
    }
}
