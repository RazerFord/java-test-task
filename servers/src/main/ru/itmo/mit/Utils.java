package ru.itmo.mit;

public class Utils {
    public static void run(RunnableWrapper runnableWrapper) {
        try {
            runnableWrapper.run();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    public interface RunnableWrapper {
        void run() throws Throwable;
    }
}
