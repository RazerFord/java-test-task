package ru.mit.itmo.guard;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultGuard implements Guard {
    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final AtomicInteger currCount = new AtomicInteger(0);
    private final Semaphore connectSemaphore = new Semaphore(1);
    private final AtomicBoolean broken = new AtomicBoolean(false);
    private final int count;

    public DefaultGuard(int count) {
        this.count = count;
    }

    public void acquire() throws InterruptedException {
        connectSemaphore.acquire();
    }

    public void release() {
        connectSemaphore.release();
    }

    @Override
    public void await() throws InterruptedException, BrokenBarrierException {
        lock.lock();
        try {
            if (broken.get()) {
                throw new BrokenBarrierException();
            }
            currCount.incrementAndGet();
            while (currCount.get() < count) {
                condition.await();
            }
            condition.signalAll();
            if (broken.get()) {
                throw new BrokenBarrierException();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void destroy() {
        lock.lock();
        try {
            broken.set(true);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
