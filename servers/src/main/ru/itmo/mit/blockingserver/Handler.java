package ru.itmo.mit.blockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Handler.class.getName());
    private final MessageReader messageReader = new MessageReader();
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
                var message = messageReader.read(inputStream);
                executorService.execute(() -> handle(message.getNumberList(), outputStream, sender));
            }
        } catch (IOException | ServerException | RejectedExecutionException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void handle(List<Integer> numbers, OutputStream outputStream, @NotNull ExecutorService sender) {
        var numbers1 = new ArrayList<>(numbers);
        Utils.bubbleSort(numbers1);
        sender.execute(() -> {
            MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
            final int size = message.getSerializedSize();
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
            byteBuffer.putInt(size).put(message.toByteArray());
            Utils.run(() -> outputStream.write(byteBuffer.array()));
        });
    }
}
