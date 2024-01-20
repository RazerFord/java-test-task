package ru.itmo.mit.asyncserver;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.Utils;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class AsyncHandler {
    private static final int FACTOR = 2;
    private static final int INITIAL_READ_BUFFER_SIZE = 1024;
    private static final int INITIAL_WRITE_BUFFER_SIZE = 1024;
    private int sizeMessage = -1;
    private ByteBuffer readBuffer = ByteBuffer.allocate(INITIAL_READ_BUFFER_SIZE);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(INITIAL_WRITE_BUFFER_SIZE);
    private final ExecutorService executorService;
    private final AsynchronousServerSocketChannel asyncServerSocketChannel;
    private final AsyncServer asyncServer;
    private final StatisticsRecorder statisticsRecorder;
    private AsynchronousSocketChannel asyncSocketChannel;

    public AsyncHandler(
            ExecutorService executorService,
            AsynchronousServerSocketChannel asyncServerSocketChannel,
            AsyncServer asyncServer,
            StatisticsRecorder statisticsRecorder
    ) {
        this.executorService = executorService;
        this.asyncServerSocketChannel = asyncServerSocketChannel;
        this.asyncServer = asyncServer;
        this.statisticsRecorder = statisticsRecorder;
    }

    public void asyncRead() {
        asyncSocketChannel.read(readBuffer, this, ReadCallback.INSTANCE);
    }

    public void asyncWrite() {
        asyncSocketChannel.write(writeBuffer, this, WriteCallback.INSTANCE);
    }

    public void handleRead() throws InvalidProtocolBufferException {
        readBuffer.flip();
        var message = MessageOuterClass.Message.parseFrom(readBuffer);
        var start = Instant.now();
        readBuffer.position(sizeMessage);
        readBuffer.compact();
        sizeMessage = -1;
        executorService.execute(() -> handleRead(
                message.getNumberList(),
                Utils.createActionAfterCompletion(statisticsRecorder, start)
        ));
    }

    public void handleWrite() {
        writeBuffer.clear();
        asyncRead();
    }

    public boolean checkRead() {
        int readBytes = readBuffer.position();
        if (!readBuffer.hasRemaining() || sizeMessage > readBytes) {
            increaseReadBufferInWriteMode();
        }

        if (sizeMessage == -1) {
            if (readBytes < Integer.BYTES) return false;
            sizeMessage = readBuffer.flip().getInt();
            readBuffer.compact();
        }

        return messageReady();
    }

    public boolean checkWrite() {
        return !writeBuffer.hasRemaining();
    }

    public AsyncHandler copy() {
        return new AsyncHandler(executorService, asyncServerSocketChannel, asyncServer, statisticsRecorder);
    }

    private void handleRead(List<Integer> numbers, Runnable runnable) {
        var numbers1 = new ArrayList<>(numbers);
        Utils.executeAndMeasureResults(() -> Utils.bubbleSort(numbers1), statisticsRecorder, StatisticsRecorder.SELECTOR_PROCESSING_REQUEST);
        MessageOuterClass.Message message = MessageOuterClass.Message.newBuilder().addAllNumber(numbers1).build();
        final int size = message.getSerializedSize();
        while (writeBuffer.capacity() < size + Integer.BYTES) increaseWriteBufferInWriteMode();
        writeBuffer.putInt(size).put(message.toByteArray());
        writeBuffer.flip();
        runnable.run();
        asyncWrite();
    }

    private boolean messageReady() {
        return sizeMessage != -1 && sizeMessage <= readBuffer.position();
    }

    public AsynchronousServerSocketChannel getAsyncServerSocketChannel() {
        return asyncServerSocketChannel;
    }

    public void setAsyncSocketChannel(AsynchronousSocketChannel asyncSocketChannel) {
        this.asyncSocketChannel = asyncSocketChannel;
    }

    private void increaseReadBufferInWriteMode() {
        int newSizeBuffer = FACTOR * readBuffer.limit();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(readBuffer.array(), newSizeBuffer));
        newByteBuffer.position(readBuffer.position());
        readBuffer = newByteBuffer;
    }

    private void increaseWriteBufferInWriteMode() {
        int newSizeBuffer = FACTOR * writeBuffer.capacity();
        ByteBuffer newByteBuffer = ByteBuffer.wrap(Arrays.copyOf(writeBuffer.array(), newSizeBuffer));
        newByteBuffer.position(writeBuffer.position());
        writeBuffer = newByteBuffer;
    }
}
