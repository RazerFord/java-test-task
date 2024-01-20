package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class StatisticsRecorder {
    private final FileEntries fileEntries = FileEntries.create();
    private final AtomicInteger value = new AtomicInteger();

    public void updateValue(int value) {
        this.value.set(value);
    }

    public void addRecord(long time, @NotNull Selector selector) {
        addRecord(selector.get(fileEntries), value.get(), time);
    }

    public static final Selector SELECTOR_PROCESSING_REQUEST = FileEntries::processingRequest;
    public static final Selector SELECTOR_PROCESSING_CLIENT = FileEntries::processingClient;
    public static final Selector SELECTOR_AVG_REQ_PROCESSING_TIME = FileEntries::averageRequestProcessingTime;

    @FunctionalInterface
    public interface Selector {
        Queue<String> get(FileEntries fileEntries);
    }

    private void addRecord(@NotNull Queue<String> queue, int value, long time) {
        queue.add(String.format(RECORDING_FORMAT, value, time));
    }

    private static final String RECORDING_FORMAT = "%s %s";
}
