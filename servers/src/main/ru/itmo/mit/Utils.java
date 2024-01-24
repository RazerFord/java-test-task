package ru.itmo.mit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static void run(RunnableWrapper runnableWrapper) {
        try {
            runnableWrapper.run();
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    public interface RunnableWrapper {
        void run() throws Exception;
    }

    public static @NotNull ByteBuffer increaseBuffer(int factor, @NotNull ByteBuffer byteBuffer) {
        int newSizeBuffer = factor * byteBuffer.capacity();
        ByteBuffer newByte = ByteBuffer.wrap(Arrays.copyOf(byteBuffer.array(), newSizeBuffer));
        return newByte.position(byteBuffer.position());
    }

    public static void bubbleSort(@NotNull List<Integer> numbers) {
        for (int j = 0; j < numbers.size(); j++) {
            for (int i = 1; i < numbers.size() - j; i++) {
                Integer left = numbers.get(i - 1);
                Integer right = numbers.get(i);
                if (left > right) {
                    numbers.set(i - 1, right);
                    numbers.set(i, left);
                }
            }
        }
    }

    @Contract(pure = true)
    public static @NotNull Runnable createActionAfterCompletion(
            StatisticsRecorder statisticsRecorder,
            Instant start
    ) {
        Runnable[] runnable = new Runnable[1];
        runnable[0] = () -> {
            var end = Instant.now();
            statisticsRecorder.addRecord(Duration.between(start, end).toMillis(), StatisticsRecorder.SELECTOR_PROCESSING_CLIENT);
            runnable[0] = () -> {
            };
        };
        return runnable[0];
    }

    public static void executeAndMeasureResults(
            @NotNull Runnable runnable,
            @NotNull StatisticsRecorder statisticsRecorder
    ) {
        var start = Instant.now();
        runnable.run();
        var end = Instant.now();
        statisticsRecorder.addRecord(Duration.between(start, end).toMillis(), StatisticsRecorder.SELECTOR_PROCESSING_REQUEST);
    }
}
