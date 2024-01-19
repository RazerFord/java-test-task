package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static void run(RunnableWrapper runnableWrapper) {
        try {
            runnableWrapper.run();
        } catch (Exception t) {
            throw new RuntimeException(t);
        }
    }

    @FunctionalInterface
    public interface RunnableWrapper {
        void run() throws Exception;
    }

    public static @NotNull ByteBuffer increaseBuffer(int factor, @NotNull ByteBuffer byteBuffer) {
        int newSizeBuffer = factor * byteBuffer.capacity();
        ByteBuffer newByte = ByteBuffer.wrap(Arrays.copyOf(byteBuffer.array(), newSizeBuffer));
        return newByte.position(byteBuffer.position());
    }

    public static void bubbleSort(@NotNull List<Integer> numbers) {
        for (int j = 0; j < numbers.size(); j++) {
            for (int i = 1; i < numbers.size() - j; i++) {
                Integer left = numbers.get(i - 1);
                Integer right = numbers.get(i);
                if (left > right) {
                    numbers.set(i - 1, right);
                    numbers.set(i, left);
                }
            }
        }
    }
}
