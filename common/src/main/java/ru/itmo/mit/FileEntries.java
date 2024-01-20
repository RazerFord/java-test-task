package ru.itmo.mit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public record FileEntries(
        Queue<String> processingRequest,
        Queue<String> processingClient,
        Queue<String> averageRequestProcessingTime
) {
    @Contract(" -> new")
    public static @NotNull FileEntries create() {
        return new FileEntries(new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>(), new ConcurrentLinkedQueue<>());
    }
}
