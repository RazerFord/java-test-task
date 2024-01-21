package ru.mit.itmo.guard;

public interface Guard {
    void await() throws InterruptedException;

    void reset();
}
