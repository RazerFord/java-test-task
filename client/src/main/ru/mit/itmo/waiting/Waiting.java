package ru.mit.itmo.waiting;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public interface Waiting {
    void trySleep() throws InterruptedException;

    void update(Duration lastRequestTime);

    static void trySleep(@NotNull Duration lastTime, Duration period) throws InterruptedException {
        Duration currTime = Duration.ofMillis(System.currentTimeMillis());
        Duration endSleepTime = lastTime.plus(period);
        Duration diff = endSleepTime.minus(currTime);
        while (diff.isPositive()) {
            Thread.sleep(diff);
            currTime = Duration.ofMillis(System.currentTimeMillis());
            diff = endSleepTime.minus(currTime);
        }
    }
}
