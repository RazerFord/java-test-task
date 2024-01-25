package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class StatisticsRecorder {
    private final AtomicReference<StatisticsRecorderStrategy> strategy = new AtomicReference<>(PASSIVE_STRATEGY);

    public boolean isActive() {
        return strategy.get() == ACTIVE_STRATEGY;
    }

    public boolean isPassive() {
        return strategy.get() == PASSIVE_STRATEGY;
    }

    public boolean isBroken() {
        return strategy.get() == BROKEN_STRATEGY;
    }

    public void makeActive() {
        strategy.set(ACTIVE_STRATEGY);
    }

    public void makePassive() {
        strategy.set(PASSIVE_STRATEGY);
    }

    public void makeBroken() {
        strategy.set(BROKEN_STRATEGY);
    }

    public void addDelta(long time, AtomicLong value) {
        strategy.get().addDelta(time, value);
    }

    public void addDeltaAndOne(long time, AtomicLong value, AtomicLong count) {
        strategy.get().addDeltaAndOne(time, value, count);
    }

    public int average(@NotNull Queue<Long> queue) {
        var size = queue.size();
        var skip = (size > 5) ? size / 5 : 0;
        var limit = (size > 5) ? skip * 3 : size;
        return (int) queue.stream().skip(skip).limit(limit).mapToLong(Long::longValue).average().orElse(0);
    }


    private interface StatisticsRecorderStrategy {

        void addDelta(long time, AtomicLong value);

        void addDeltaAndOne(long time, AtomicLong value, AtomicLong count);
    }

    private static final ActiveStrategy ACTIVE_STRATEGY = new ActiveStrategy();

    private static final PassiveStrategy PASSIVE_STRATEGY = new PassiveStrategy();

    private static final PassiveStrategy BROKEN_STRATEGY = new PassiveStrategy();

    private static class ActiveStrategy implements StatisticsRecorderStrategy {
        @Override
        public void addDelta(long time, @NotNull AtomicLong value) {
            value.addAndGet(time);
        }

        @Override
        public void addDeltaAndOne(long time, @NotNull AtomicLong value, @NotNull AtomicLong count) {
            value.addAndGet(time);
            count.incrementAndGet();
        }
    }

    private static class PassiveStrategy implements StatisticsRecorderStrategy {
        @Override
        public void addDelta(long time, AtomicLong value) {
            // this code block must be empty
        }

        @Override
        public void addDeltaAndOne(long time, AtomicLong value, AtomicLong count) {
            // this code block must be empty
        }
    }
}
