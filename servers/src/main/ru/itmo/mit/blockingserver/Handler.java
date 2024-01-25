package ru.itmo.mit.blockingserver;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Handler implements Runnable, Result {
    private static final Logger LOGGER = Logger.getLogger(Handler.class.getName());
    private final MessageReader messageReader = new MessageReader();
    private final Pair<AtomicLong, AtomicLong> requestProcTimeAndCount = createPair();
    private final Pair<AtomicLong, AtomicLong> clientProcTimeAndCount = createPair();
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
                var pair = messageReader.read(inputStream);
                var message = pair.first();
                var start = pair.second();
                executorService.execute(() -> handle(message.getNumberList(),
                        outputStream,
                        sender,
                        Utils.createActionAfterCompletion(statisticsRecorder, start, clientProcTimeAndCount))
                );
            }
        } catch (IOException | ServerException | RejectedExecutionException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void handle(
            List<Integer> numbers,
            OutputStream outputStream,
            @NotNull ExecutorService sender,
            Runnable actionAfterCompletion
    ) {
        var numbers1 = new ArrayList<>(numbers);
        Utils.executeAndMeasureResults(
                () -> Utils.bubbleSort(numbers1),
                statisticsRecorder,
                requestProcTimeAndCount
        );
        sender.execute(() -> {
            MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
            final int size = message.getSerializedSize();
            ByteBuffer byteBuffer = ByteBuffer.allocate(Integer.BYTES + size);
            byteBuffer.putInt(size).put(message.toByteArray());
            actionAfterCompletion.run();
            Utils.run(() -> outputStream.write(byteBuffer.array()));
        });
    }

    @Override
    public int getRequestProcessingTime() {
        return calculate(requestProcTimeAndCount);
    }

    @Override
    public int getClientProcessingTime() {
        return calculate(clientProcTimeAndCount);
    }

    @Contract(" -> new")
    private static @NotNull Pair<AtomicLong, AtomicLong> createPair() {
        return new Pair<>(new AtomicLong(0), new AtomicLong(0));
    }

    private static int calculate(@NotNull Pair<AtomicLong, AtomicLong> pair) {
        return (int) (pair.first().get() / pair.second().get());
    }
}
