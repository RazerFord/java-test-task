package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class DefaultGuard implements Guard {
    private final CyclicBarrier cyclicBarrier;

    public DefaultGuard(int count) {
        cyclicBarrier = new CyclicBarrier(count);
    }

    @Override
    public void await() throws InterruptedException {
        try {
            cyclicBarrier.await();
        } catch (BrokenBarrierException e) {
            throw new InterruptedException();
        }
    }
}
