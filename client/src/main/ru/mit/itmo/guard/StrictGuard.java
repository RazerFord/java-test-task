package ru.mit.itmo.guard;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class StrictGuard implements Guard {
    private final Semaphore semaphore = new Semaphore(0);
    private final int to;
    private final AtomicInteger active;
    private final Semaphore beginSemaphore;
    private final CyclicBarrier barrier;
    private Runnable release;

    public StrictGuard(int from, int to, int step) {
        this.to = to;
        active = new AtomicInteger(from);
        beginSemaphore = new Semaphore(from);
        barrier = new CyclicBarrier(step);
        release = createRelease();
    }

    @Override
    public void acquire() throws InterruptedException {
        if (beginSemaphore.tryAcquire()) {
            return;
        }
        semaphore.acquire();
        active.incrementAndGet();
        if (active.get() > to) {
            throw new InterruptedException();
        }
        if (active.get() == to) {
            barrier.reset();
            return;
        }
        try {
            barrier.await();
        } catch (BrokenBarrierException e) {
            // this code block should be empty
        }
    }

    @Override
    public void release() {
        release.run();
    }

    @Contract(pure = true)
    private @NotNull Runnable createRelease() {
        return () -> {
            if (active.get() < to || semaphore.getQueueLength() > 0) semaphore.release();
            else release = () -> {
            };
        };
    }
}
