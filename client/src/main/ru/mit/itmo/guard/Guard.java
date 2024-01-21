package ru.mit.itmo.guard;

public interface Guard {

    void acquire() throws InterruptedException;

    void release();

    void await() throws InterruptedException;

    void reset();
}
