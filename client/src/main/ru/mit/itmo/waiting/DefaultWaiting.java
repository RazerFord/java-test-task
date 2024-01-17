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
        Duration currTime = Duration.ofMillis(System.currentTimeMillis());
        Duration endSleepTime = lastTime.plus(period);
        Duration diff = endSleepTime.minus(currTime);
        while (diff.isPositive()) {
            Thread.sleep(diff);
            currTime = Duration.ofMillis(System.currentTimeMillis());
            diff = endSleepTime.minus(currTime);
        }
    }

    @Override
    public void update(Duration lastRequestTime) {
        this.lastTime = lastRequestTime;
    }
}
