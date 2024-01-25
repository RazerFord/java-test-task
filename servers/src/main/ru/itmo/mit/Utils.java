package ru.itmo.mit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

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

    @Contract(" -> new")
    public static @NotNull Pair<AtomicLong, AtomicLong> createAtomicLongPair() {
        return new Pair<>(new AtomicLong(0), new AtomicLong(0));
    }

    @Contract(pure = true)
    public static @NotNull Runnable createActionAfterCompletion(
            StatisticsRecorder statisticsRecorder,
            Instant start,
            Pair<AtomicLong, AtomicLong> clientProcTimeAndCount
    ) {
        Runnable[] runnable = new Runnable[1];
        runnable[0] = () -> {
            var diff = Duration.between(start, Instant.now()).toMillis();
            statisticsRecorder.addDeltaAndOne(diff, clientProcTimeAndCount.first(), clientProcTimeAndCount.second());
            runnable[0] = () -> {
            };
        };
        return runnable[0];
    }

    public static void executeAndMeasureResults(
            @NotNull Runnable runnable,
            @NotNull StatisticsRecorder statisticsRecorder,
            @NotNull Pair<AtomicLong, AtomicLong> requestProcTimeAndCount
    ) {
        var start = Instant.now();
        runnable.run();
        var diff = Duration.between(start, Instant.now()).toMillis();
        statisticsRecorder.addDeltaAndOne(diff, requestProcTimeAndCount.first(), requestProcTimeAndCount.second());
    }
}
