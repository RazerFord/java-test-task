package ru.itmo.mit.blockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.Utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
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
    private final StatisticsRecorder statisticsRecorder;

    public Handler(Socket socket, ExecutorService executorService, StatisticsRecorder statisticsRecorder) {
        this.socket = socket;
        this.executorService = executorService;
        this.statisticsRecorder = statisticsRecorder;
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
                var start = Instant.now();
                var message = messageReader.read(inputStream);
                executorService.execute(() -> handle(message.getNumberList(),
                        outputStream,
                        sender,
                        Utils.createActionAfterCompletion(statisticsRecorder, start))
                );
            }
        } catch (IOException | ServerException | RejectedExecutionException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void handle(List<Integer> numbers, OutputStream outputStream, @NotNull ExecutorService sender, Runnable actionAfterCompletion) {
        var numbers1 = new ArrayList<>(numbers);
        var start = Instant.now();
        Utils.bubbleSort(numbers1);
        var end = Instant.now();
        statisticsRecorder.addRecord(Duration.between(start, end).toMillis(), StatisticsRecorder.SELECTOR_PROCESSING_REQUEST);
        sender.execute(() -> {
            MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
            final int size = message.getSerializedSize();
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
            byteBuffer.putInt(size).put(message.toByteArray());
            actionAfterCompletion.run();
            Utils.run(() -> outputStream.write(byteBuffer.array()));
        });
    }
}
