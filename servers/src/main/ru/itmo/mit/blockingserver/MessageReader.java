package ru.itmo.mit.blockingserver;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.ServerException;
import ru.itmo.mit.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class MessageReader {
    private static final int FACTOR = 2;
    private static final int INITIAL_BUFFER_SIZE = 1;
    private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);

    public MessageOuterClass.@NotNull Message read(InputStream inputStream) throws IOException {
        int totalRead = buffer.position();
        totalRead += readAtLeastNBytes(inputStream, totalRead, Integer.BYTES);
        buffer.position(totalRead);
        var size = buffer.flip().getInt() + Integer.BYTES;
        totalRead += readAtLeastNBytes(inputStream, totalRead, size);
        buffer.position(Integer.BYTES);
        buffer.limit(size);
        MessageOuterClass.Message message = MessageOuterClass.Message.parseFrom(buffer);
        buffer.position(size);
        buffer.compact();
        buffer.position(totalRead - size);
        return message;
    }

    private int readAtLeastNBytes(InputStream inputStream, int readBytes, int countBytes) throws IOException {
        int oldReadBytes = readBytes;
        while (readBytes < countBytes) {
            int read = inputStream.read(buffer.array(), readBytes, buffer.capacity() - readBytes);
            if (read == -1) throw END_STREAM;
            if (read == 0) increaseBuffer();
            readBytes += read;
        }
        return readBytes - oldReadBytes;
    }

    private void increaseBuffer() {
        buffer = Utils.increaseBuffer(FACTOR, buffer);
    }

    private static final ServerException END_STREAM = new ServerException("End of stream reached");
}
