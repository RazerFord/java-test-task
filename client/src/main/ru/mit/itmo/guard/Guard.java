package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;

public interface Guard {

    void acquire() throws InterruptedException;

    void release();

    void await() throws InterruptedException, BrokenBarrierException;

    void destroy();
}
