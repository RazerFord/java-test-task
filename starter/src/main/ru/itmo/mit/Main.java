package ru.itmo.mit;

import ru.itmo.mit.blockingserver.BlockingServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.List;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws IOException {
        new Thread(() -> {
            try {
                new BlockingServer(8081).start();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        int i = 0;
        try (Socket socket = new Socket("localhost", 8081);
             InputStream inputStream = socket.getInputStream();
             OutputStream outputStream = socket.getOutputStream()) {
            while (i++ < 10) {
                MessageOuterClass.Message message = MessageOuterClass
                        .Message
                        .newBuilder()
                        .addAllNumber(List.of(234, 12, 53, 2, 5, 12, 5, 123, 5))
                        .build();

                int size = message.getSerializedSize();
                System.out.println(size);

                outputStream.write(
                        ByteBuffer.allocate(Integer.BYTES + size)
                                .putInt(size)
                                .put(message.toByteArray())
                                .array()
                );

                size = Integer.BYTES;


                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                if (!read(inputStream, byteBuffer, size)) return;

                size = byteBuffer.getInt();

                byteBuffer = ByteBuffer.allocate(size);
                if (!read(inputStream, byteBuffer, size)) return;
                message = MessageOuterClass.Message.parseFrom(byteBuffer);
                for (int f : message.getNumberList()) {
                    System.out.printf("%s ", f);
                }
            }
        }
        System.out.println("END");
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
}