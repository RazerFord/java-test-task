package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

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

    public static @NotNull ByteBuffer increaseBuffer(int factor, @NotNull ByteBuffer byteBuffer) {
        int newSizeBuffer = factor * byteBuffer.capacity();
        ByteBuffer newByte = ByteBuffer.wrap(Arrays.copyOf(byteBuffer.array(), newSizeBuffer));
        return newByte.position(byteBuffer.position());
    }
}
