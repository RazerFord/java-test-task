package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class DefaultGuard implements Guard {
    private final CyclicBarrier cyclicBarrier;
    private final Semaphore connectSemaphore = new Semaphore(1);

    public DefaultGuard(int count) {
        cyclicBarrier = new CyclicBarrier(count);
    }

    public void acquire() throws InterruptedException {
        connectSemaphore.acquire();
    }

    public void release() {
        connectSemaphore.release();
    }

    @Override
    public void await() throws InterruptedException {
        try {
            cyclicBarrier.await();
        } catch (BrokenBarrierException e) {
            throw new InterruptedException();
        }
    }

    @Override
    public void reset() {
        cyclicBarrier.reset();
    }
}
