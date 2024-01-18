package ru.itmo.mit;

import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.IncreasingArrayGenerators;
import ru.mit.itmo.waiting.DefaultWaiting;
import ru.mit.itmo.waiting.IncreasingWaiting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Arrays;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        var server = new BlockingServer(8081);
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        var gen = new IncreasingArrayGenerators(10, 10000, 100);
        var waiting = new DefaultWaiting(Duration.ZERO);

        Thread[] threads = new Thread[10];

        for (int i = 0; i < 10; i++) {
            Client client = new Client("0.0.0.0", 8081, gen, 10, waiting);
            var t = new Thread(client);
            threads[i] = t;
            t.start();
        }
        for (int i = 0; i < 1; i++) {
            threads[i].join();
        }

        try {
            server.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean read(InputStream inputStream, ByteBuffer byteBuffer, int size) throws IOException {
        int totalReadBytes = 0;
        while (totalReadBytes != size) {
            int readBytes = inputStream.read(byteBuffer.array());
            if (readBytes == -1) return false;
            totalReadBytes += readBytes;
        }
        return true;
    }

    static private ByteBuffer increaseReadBuffer(ByteBuffer readBuffer) {
        int newSizeBuffer = 2 * readBuffer.limit();
        byte[] bytes = Arrays.copyOf(readBuffer.array(), newSizeBuffer);
        ByteBuffer newByteBuffer = ByteBuffer.wrap(bytes);
        newByteBuffer.position(readBuffer.position());
        newByteBuffer.limit(readBuffer.limit());
        return newByteBuffer;
    }
}