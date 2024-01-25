package ru.mit.itmo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.arraygenerators.ArrayGenerators;
import ru.mit.itmo.exceptions.ClientException;
import ru.mit.itmo.exceptions.ReconnectionException;
import ru.mit.itmo.guard.Guard;
import ru.mit.itmo.waiting.Waiting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private static final long DURATION_SLEEP_BEFORE_CONNECTION = 1000; // ms
    private static final long RECONNECTION_LIMIT = 15;
    private final AtomicBoolean acquired = new AtomicBoolean(false);
    private final AtomicLong connectionAttempt = new AtomicLong(1);
    private final MessageReader messageReader = new MessageReader();
    private final MessageSender messageSender = new MessageSender();
    private final AtomicLong averageRequestTime = new AtomicLong(0);
    private final String targetAddress;
    private final int targetPort;
    private final int countRequest;
    private final ArrayGenerators arrayGenerators;
    private final Waiting waiting;
    private final Guard guard;
    private final StatisticsRecorder statisticsRecorder;

    private Client(
            String targetAddress,
            int targetPort,
            int countRequest,
            ArrayGenerators arrayGenerators,
            Waiting waiting,
            Guard guard,
            StatisticsRecorder statisticsRecorder
    ) {
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
        this.countRequest = countRequest;
        this.arrayGenerators = arrayGenerators;
        this.waiting = waiting;
        this.guard = guard;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void run() {
        try {
            tryToDo();
        } catch (BrokenBarrierException | IOException | InterruptedException | ClientException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            Thread.currentThread().interrupt();
            guard.destroy();
            statisticsRecorder.makeBroken();
        }
    }

    private void tryToDo() throws InterruptedException, IOException, BrokenBarrierException {
        acquire();
        try (
                var socket = new Socket(targetAddress, targetPort);
                var outputStream = socket.getOutputStream();
                var inputStream = socket.getInputStream()
        ) {
            release();
            guard.await();
            statisticsRecorder.makeActive();
            processClient(outputStream, inputStream);
        } catch (BindException | ConnectException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            long i = connectionAttempt.getAndIncrement();
            if (i == RECONNECTION_LIMIT) {
                throw new ReconnectionException();
            }
            Thread.sleep(i * DURATION_SLEEP_BEFORE_CONNECTION);
            release();
            tryToDo();
        } finally {
            statisticsRecorder.makePassive();
            release();
        }
    }

    private void processClient(OutputStream outputStream, InputStream inputStream) throws InterruptedException, IOException {
        var numberRequestsActiveMode = new AtomicLong(0);
        for (int i = 0; i < countRequest; i++) {
            var start = Instant.now();
            waiting.trySleep();
            var message = buildRequest();
            messageSender.send(message, outputStream);
            message = messageReader.read(inputStream);
            waiting.update(Duration.ofMillis(System.currentTimeMillis()));
            checkSortingList(message.getNumberList());
            var diff = Duration.between(start, Instant.now()).toMillis();
            statisticsRecorder.addDeltaAndOne(diff, averageRequestTime, numberRequestsActiveMode);
        }
        if (numberRequestsActiveMode.get() == 0) return;
        averageRequestTime.set(averageRequestTime.get() / numberRequestsActiveMode.get());
    }

    public long getAverageRequestTime() {
        return averageRequestTime.get();
    }

    public void addIfNonZeroAverageRequestTime(@NotNull Queue<Long> queue) {
        if (averageRequestTime.get() == 0) return;
        queue.add(averageRequestTime.get());
    }

    private MessageOuterClass.@NotNull Message buildRequest() {
        List<Integer> numbers = arrayGenerators.generate();
        return MessageOuterClass.Message.newBuilder().addAllNumber(numbers).build();
    }

    private void acquire() throws InterruptedException {
        acquired.set(true);
        guard.acquire();
    }

    private void release() {
        if (acquired.get()) {
            guard.release();
            acquired.set(false);
        }
    }

    private void checkSortingList(@NotNull List<Integer> numbers) {
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i - 1) > numbers.get(i)) {
                throw LIST_NOT_SORTED;
            }
        }
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private String targetAddress;
        private int targetPort = -1;
        private int countRequest = -1;
        private Supplier<ArrayGenerators> arrayGenerators;
        private Supplier<Waiting> waiting;
        private Supplier<Guard> guard;
        private Supplier<StatisticsRecorder> statisticsRecorder;

        private Builder() {
        }

        public Builder setTargetAddress(String targetAddress) {
            Objects.requireNonNull(targetAddress);
            this.targetAddress = targetAddress;
            return this;
        }

        public Builder setTargetPort(int targetPort) {
            this.targetPort = targetPort;
            return this;
        }

        public Builder setCountRequest(int countRequest) {
            this.countRequest = countRequest;
            return this;
        }

        public Builder setArrayGeneratorsSupplier(Supplier<ArrayGenerators> arrayGenerators) {
            Objects.requireNonNull(arrayGenerators);
            this.arrayGenerators = arrayGenerators;
            return this;
        }

        public Builder setWaitingSupplier(Supplier<Waiting> waiting) {
            Objects.requireNonNull(waiting);
            this.waiting = waiting;
            return this;
        }

        public Builder setGuardSupplier(Supplier<Guard> guard) {
            Objects.requireNonNull(guard);
            this.guard = guard;
            return this;
        }

        public Builder setStatisticsRecorderSupplier(Supplier<StatisticsRecorder> statisticsRecorder) {
            Objects.requireNonNull(statisticsRecorder);
            this.statisticsRecorder = statisticsRecorder;
            return this;
        }

        public Client build() {
            return new Client(
                    targetAddress,
                    targetPort,
                    countRequest,
                    Objects.requireNonNull(arrayGenerators.get()),
                    Objects.requireNonNull(waiting.get()),
                    Objects.requireNonNull(guard.get()),
                    Objects.requireNonNull(statisticsRecorder.get())
            );
        }
    }

    private static final ClientException LIST_NOT_SORTED = new ClientException("The list is not sorted");
}
