package ru.itmo.mit.blockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Handler implements Runnable {
    private final Socket socket;
    private final ExecutorService executorService;

    public Handler(Socket socket, ExecutorService executorService) {
        this.socket = socket;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try (
                var socket1 = socket;
                var inputStream = socket1.getInputStream();
                var outputStream = socket1.getOutputStream();
                var sender = Executors.newSingleThreadExecutor()
        ) {
            while (!socket1.isClosed() && !Thread.currentThread().isInterrupted()) {
                int size = Integer.BYTES;

                ByteBuffer byteBuffer = ByteBuffer.allocate(size);
                if (!read(inputStream, byteBuffer, size)) return;

                size = byteBuffer.getInt();

                byteBuffer = ByteBuffer.allocate(size);
                if (!read(inputStream, byteBuffer, size)) return;

                MessageOuterClass.Message message = MessageOuterClass.Message.parseFrom(byteBuffer);
                executorService.execute(() -> handle(message.getNumberList(), outputStream, sender));
            }
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    private void handle(List<Integer> numbers, OutputStream outputStream, @NotNull ExecutorService sender) {
        var numbers1 = new ArrayList<>(numbers);
        Collections.sort(numbers1);
        sender.execute(() -> {
            MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
            final int size = message.getSerializedSize();
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
            byteBuffer.putInt(size).put(message.toByteArray());
            try {
                outputStream.write(byteBuffer.array());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean read(InputStream inputStream, ByteBuffer byteBuffer, int size) throws IOException {
        int totalReadBytes = 0;
        while (totalReadBytes != size) {
            int readBytes = inputStream.read(byteBuffer.array());
            if (readBytes == -1) return false;
            totalReadBytes += readBytes;
        }
        return true;
    }
}
