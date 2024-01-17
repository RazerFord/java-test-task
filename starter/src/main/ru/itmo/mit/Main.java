package ru.itmo.mit;

import ru.itmo.mit.blockingserver.BlockingServer;
import ru.mit.itmo.Client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        var server = new BlockingServer(8081);
        new Thread(() -> {
            try {
                server.start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        Client client = new Client("0.0.0.0", 8081, 100_000, 100, 0);
        client.run();

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