package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class StrictGuard implements Guard {
    private final AtomicInteger active = new AtomicInteger();
    private final Semaphore semaphore = new Semaphore(0);
    private final int from;
    private final int to;
    private final CyclicBarrier barrier;

    public StrictGuard(int from, int to, int step) {
        this.from = from;
        this.to = to;
        barrier = new CyclicBarrier(step);
    }

    @Override
    public void acquire() throws InterruptedException, BrokenBarrierException {
        // this block can be changed to a semaphore with tryAcquire
        while (true) {
            var curActive = active.get();
            if (curActive > from) break;
            if (active.compareAndSet(curActive, curActive + 1)) return;
        }
        semaphore.acquire();
        barrier.await();
        active.incrementAndGet();
        if (active.get() > to) {
            throw new InterruptedException();
        }
    }

    @Override
    public void release() {
        semaphore.release();
    }
}
