package ru.mit.itmo.waiting;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class IncreasingWaiting implements Waiting {
    private Duration lastTime = Duration.ZERO;
    private Duration from;
    private final Duration to;
    private final Duration step;

    public IncreasingWaiting(Duration from, Duration to, Duration step) {
        this.from = from;
        this.to = to;
        this.step = step;
    }

    @Override
    public void trySleep() throws InterruptedException {
        Waiting.trySleep(lastTime, from);
        from = min(from.plus(step), to);
        System.out.println(from.getSeconds());
    }

    @Override
    public void update(Duration lastRequestTime) {
        this.lastTime = lastRequestTime;
    }

    private static Duration min(@NotNull Duration l, Duration r) {
        return l.compareTo(r) < 0 ? l : r;
    }
}
