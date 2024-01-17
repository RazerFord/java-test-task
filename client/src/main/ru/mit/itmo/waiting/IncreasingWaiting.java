package ru.mit.itmo.waiting;

import java.time.Duration;

public class IncreasingWaiting implements Waiting {
    private Duration lastRequestTime = Duration.ZERO;

    @Override
    public void trySleep() {

    }

    @Override
    public void update(Duration lastRequestTime) {
        this.lastRequestTime = lastRequestTime;
    }
}
