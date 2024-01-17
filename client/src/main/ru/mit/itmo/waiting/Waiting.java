package ru.mit.itmo.waiting;

import java.time.Duration;

public interface Waiting {
    void trySleep() throws InterruptedException;

    void update(Duration lastRequestTime);
}
