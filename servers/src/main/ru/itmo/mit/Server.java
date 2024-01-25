package ru.itmo.mit;

import java.io.Closeable;

public interface Server extends Runnable, Closeable, Result {
    int getPort() throws InterruptedException;

    void reset();
}
