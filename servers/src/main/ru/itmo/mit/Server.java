package ru.itmo.mit;

import java.io.Closeable;

public interface Server extends Runnable, Closeable {
    int getPort() throws InterruptedException;
}
