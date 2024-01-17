package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class MessageSender {
    private static final int FACTOR = 2;
    private static final int INITIAL_BUFFER_SIZE = 1024;
    private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);

    public void send(MessageOuterClass.@NotNull Message message, OutputStream outputStream) throws IOException {
        final int size = message.getSerializedSize();
        final int totalSize = Integer.BYTES + size;
        final int capacity = buffer.capacity();
        if (capacity < totalSize) increaseBuffer((totalSize + capacity - 1) / capacity);
        buffer.putInt(size).put(message.toByteArray());
        outputStream.write(buffer.array(), 0, totalSize);
        buffer.clear();
    }

    private void increaseBuffer(int factor) {
        factor = (int) Math.pow(FACTOR, Math.ceil(Math.log(factor) / Math.log(FACTOR)));
        buffer = Utils.increaseBuffer(factor, buffer);
    }
}
