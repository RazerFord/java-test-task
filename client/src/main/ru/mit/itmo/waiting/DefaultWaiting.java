package ru.mit.itmo.waiting;

import java.time.Duration;

public class DefaultWaiting implements Waiting {
    private Duration lastTime = Duration.ZERO;
    private final Duration period;

    public DefaultWaiting(Duration period) {
        this.period = period;
    }

    @Override
    public void trySleep() throws InterruptedException {
       Waiting.trySleep(lastTime, period);
    }

    @Override
    public void update(Duration lastRequestTime) {
        this.lastTime = lastRequestTime;
    }
}
