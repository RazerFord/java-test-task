package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;

public interface Guard {
    void acquire() throws InterruptedException, BrokenBarrierException;

    void release();
}
