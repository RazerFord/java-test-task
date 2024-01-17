package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final int FACTOR = 2;
    private static final int INITIAL_BUFFER_SIZE = 1024;
    private final Random random = new Random();
    private ByteBuffer buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
    private final String targetAddress;
    private final int targetPort;
    private final int countNumbers;
    private final int countRequest;
    private final int periodRequest;

    public Client(
            String targetAddress,
            int targetPort,
            int countNumbers,
            int countRequest,
            int periodRequest
    ) {
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
        this.countNumbers = countNumbers;
        this.countRequest = countRequest;
        this.periodRequest = periodRequest;
    }

    @Override
    public void run() {
        try (
                var socket = new Socket(targetAddress, targetPort);
                var outputStream = socket.getOutputStream();
                var inputStream = socket.getInputStream()
        ) {
            for (int i = 0; i < countRequest; i++) {
                var message = buildRequest();
                send(message, outputStream);
                message = read(inputStream);
                checkSortingList(message.getNumberList());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private void send(MessageOuterClass.@NotNull Message message, OutputStream outputStream) throws IOException {
        final int size = message.getSerializedSize();
        final int totalSize = Integer.BYTES + size;
        final int capacity = buffer.capacity();
        if (capacity < totalSize) increaseBuffer((totalSize + capacity - 1) / capacity);
        buffer.putInt(size).put(message.toByteArray());
        outputStream.write(buffer.array(), 0, totalSize);
        buffer.clear();
    }

    private MessageOuterClass.@NotNull Message read(InputStream inputStream) throws IOException {
        int totalRead = readAtLeastNBytes(inputStream, 0, Integer.BYTES);
        buffer.position(totalRead);
        var size = buffer.flip().getInt();
        totalRead += readAtLeastNBytes(inputStream, totalRead, size);
        buffer.position(Integer.BYTES);
        buffer.limit(totalRead);
        MessageOuterClass.Message message = MessageOuterClass.Message.parseFrom(buffer);
        buffer.clear();
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

    private MessageOuterClass.@NotNull Message buildRequest() {
        List<Integer> numbers = new ArrayList<>(countNumbers);
        for (int i = 0; i < countNumbers; i++) {
            numbers.add(random.nextInt());
        }
        return MessageOuterClass.Message.newBuilder().addAllNumber(numbers).build();
    }

    private void increaseBuffer() {
        increaseBuffer(FACTOR);
    }

    private void increaseBuffer(int factor) {
        factor = (int) Math.pow(FACTOR, Math.ceil(Math.log(factor) / Math.log(FACTOR)));
        int newSizeBuffer = factor * buffer.capacity();
        ByteBuffer newByte = ByteBuffer.wrap(Arrays.copyOf(buffer.array(), newSizeBuffer));
        newByte.position(buffer.position());
        buffer = newByte;
    }

    private void checkSortingList(@NotNull List<Integer> numbers) {
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i - 1) > numbers.get(i)) {
                throw LIST_NOT_SORTED;
            }
        }
    }

    private static final ClientException END_STREAM = new ClientException("End of stream reached");
    private static final ClientException LIST_NOT_SORTED = new ClientException("The list is not sorted");
}
