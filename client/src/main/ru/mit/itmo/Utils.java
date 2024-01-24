package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Utils {
    private Utils() {
    }

    public static @NotNull ByteBuffer increaseBuffer(int factor, @NotNull ByteBuffer byteBuffer) {
        int newSizeBuffer = factor * byteBuffer.capacity();
        ByteBuffer newByte = ByteBuffer.wrap(Arrays.copyOf(byteBuffer.array(), newSizeBuffer));
        return newByte.position(byteBuffer.position());
    }
}
