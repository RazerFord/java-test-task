package ru.mit.itmo.guard;

public class StrictGuard implements Guard {
    private int from;
    private final int to;
    private final int step;

    public StrictGuard(int from, int to, int step) {
        this.from = from;
        this.to = to;
        this.step = step;
    }

    @Override
    public void acquire() {
        // this block of code will be posted soon
    }

    @Override
    public void release() {
        // this block of code will be posted soon
    }
}
