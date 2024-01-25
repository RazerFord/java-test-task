package ru.itmo.mit;

import java.util.Queue;

public interface AddedResult {
    void addIfNotZeroRequestProcessingTime(Queue<Long> queue);

    void addIfNotZeroClientProcessingTime(Queue<Long> queue);
}
