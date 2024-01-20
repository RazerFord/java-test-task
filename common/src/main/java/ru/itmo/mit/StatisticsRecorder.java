package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class StatisticsRecorder {
    private final FileEntries fileEntries = FileEntries.create();
    private final AtomicInteger value = new AtomicInteger();
    private final AtomicReference<StatisticsRecorderStrategy> strategy = new AtomicReference<>(PASSIVE_STRATEGY);

    public void makeActive() {
        strategy.set(ACTIVE_STRATEGY);
    }

    public void makePassive() {
        strategy.set(PASSIVE_STRATEGY);
    }

    public void clear() {
        fileEntries.processingRequest().clear();
        fileEntries.processingClient().clear();
        fileEntries.averageRequestProcessingTime().clear();
    }

    public double average(@NotNull Selector selector) {
        return average(selector.get(fileEntries));
    }

    public void updateValue(int value) {
        this.value.set(value);
    }

    public void addRecord(long time, @NotNull Selector selector) {
        strategy.get().addRecord(selector.get(fileEntries), value.get(), time);
    }

    public double average(@NotNull Queue<Long> queue) {
        var size = queue.size();
        var skip = (size > 5) ? size / 5 : 0;
        var limit = (size > 5) ? skip * 3 : size;
        return queue.stream().skip(skip).limit(limit).mapToLong(Long::longValue).average().orElse(0.0);
    }

    public static final Selector SELECTOR_PROCESSING_REQUEST = FileEntries::processingRequest;
    public static final Selector SELECTOR_PROCESSING_CLIENT = FileEntries::processingClient;
    public static final Selector SELECTOR_AVG_REQ_PROCESSING_TIME = FileEntries::averageRequestProcessingTime;

    @FunctionalInterface
    public interface Selector {
        Queue<Long> get(FileEntries fileEntries);
    }

    @FunctionalInterface
    private interface StatisticsRecorderStrategy {
        void addRecord(@NotNull Queue<Long> queue, int value, long time);
    }

    private static final ActiveStrategy ACTIVE_STRATEGY = new ActiveStrategy();

    private static final PassiveStrategy PASSIVE_STRATEGY = new PassiveStrategy();

    private static class ActiveStrategy implements StatisticsRecorderStrategy {
        @Override
        public void addRecord(@NotNull Queue<Long> queue, int value, long time) {
            queue.add(time);
        }
    }

    private static class PassiveStrategy implements StatisticsRecorderStrategy {
        @Override
        public void addRecord(@NotNull Queue<Long> queue, int value, long time) {
            // this code block must be empty
        }
    }
}
