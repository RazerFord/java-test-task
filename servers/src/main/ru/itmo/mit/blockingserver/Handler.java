package ru.itmo.mit.blockingserver;

import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Handler implements Runnable {
    private final Socket socket;
    private final ExecutorService executorService;
    private final ExecutorService sender = Executors.newSingleThreadExecutor();

    public Handler(Socket socket, ExecutorService executorService) {
        this.socket = socket;
        this.executorService = executorService;
    }

    @Override
    public void run() {
        try (Socket socket1 = socket) {
//            while (!socket1.isClosed() && !Thread.currentThread().isInterrupted()) {
                ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES);
                int reading = socket1.getInputStream().read(byteBuffer.array());
                int size = byteBuffer.getInt();

                System.out.printf("read %s\n", size);
                byteBuffer = ByteBuffer.allocate(size);
                reading = socket1.getInputStream().read(byteBuffer.array());
                MessageOuterClass.Message message = MessageOuterClass.Message.parseFrom(byteBuffer);
                for (var i : message.getNumberList()) {
                    System.out.println(i);
                }
//            }
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }
}
