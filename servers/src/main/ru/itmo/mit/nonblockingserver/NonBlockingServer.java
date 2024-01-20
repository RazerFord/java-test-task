package ru.itmo.mit.nonblockingserver;

import ru.itmo.mit.Server;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class NonBlockingServer implements Server {
    private static final Logger LOGGER = Logger.getLogger(NonBlockingServer.class.getName());
    private static final int NUMBER_THREADS = 10;
    private final SocketAddress inetAddress;
    private final int numberThreads;
    private final StatisticsRecorder statisticsRecorder;
    private ServerSocketChannel socketChannel;
    private boolean closed;

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

    public void run(ServerSocketChannel socketChannel) throws IOException {
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
}
