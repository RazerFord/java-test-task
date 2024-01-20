package ru.mit.itmo.guard;

public class DefaultGuard implements Guard {
    public static final DefaultGuard INSTANCE = new DefaultGuard();

    @Override
    public void acquire() {
       // this code block should remain empty
    }

    @Override
    public void release() {
        // this code block should remain empty
    }
}
