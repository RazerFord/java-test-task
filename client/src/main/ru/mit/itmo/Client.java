package ru.mit.itmo;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.arraygenerators.ArrayGenerators;
import ru.mit.itmo.guard.Guard;
import ru.mit.itmo.waiting.Waiting;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final MessageReader messageReader = new MessageReader();
    private final MessageSender messageSender = new MessageSender();
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
            guard.await();
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            Thread.currentThread().interrupt();
            return;
        }
        var start = Instant.now();
        try (
                var socket = new Socket(targetAddress, targetPort);
                var outputStream = socket.getOutputStream();
                var inputStream = socket.getInputStream()
        ) {
            for (int i = 0; i < countRequest; i++) {
                waiting.trySleep();
                var message = buildRequest();
                messageSender.send(message, outputStream);
                message = messageReader.read(inputStream);
                waiting.update(Duration.ofMillis(System.currentTimeMillis()));
                checkSortingList(message.getNumberList());
            }
        } catch (IOException | InterruptedException | ClientException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
            Thread.currentThread().interrupt();
        }
        var end = Instant.now();
        statisticsRecorder.addRecord(Duration.between(start, end).toMillis(), StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME);
    }

    private MessageOuterClass.@NotNull Message buildRequest() {
        List<Integer> numbers = arrayGenerators.generate();
        return MessageOuterClass.Message.newBuilder().addAllNumber(numbers).build();
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

    public static class Builder {
        private String targetAddress;
        private int targetPort;
        private int countRequest;
        private Supplier<ArrayGenerators> arrayGenerators;
        private Supplier<Waiting> waiting;
        private Supplier<Guard> guard;
        private Supplier<StatisticsRecorder> statisticsRecorder;

        private Builder() {
        }

        public Builder setTargetAddress(String targetAddress) {
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
            this.arrayGenerators = arrayGenerators;
            return this;
        }

        public Builder setWaitingSupplier(Supplier<Waiting> waiting) {
            this.waiting = waiting;
            return this;
        }

        public Builder setGuardSupplier(Supplier<Guard> guard) {
            this.guard = guard;
            return this;
        }

        public Builder setStatisticsRecorderSupplier(Supplier<StatisticsRecorder> statisticsRecorder) {
            this.statisticsRecorder = statisticsRecorder;
            return this;
        }

        public Client build() {
            return new Client(
                    Objects.requireNonNull(targetAddress),
                    targetPort,
                    countRequest,
                    Objects.requireNonNull(arrayGenerators).get(),
                    Objects.requireNonNull(waiting).get(),
                    Objects.requireNonNull(guard).get(),
                    Objects.requireNonNull(statisticsRecorder).get()
            );
        }
    }

    private static final ClientException LIST_NOT_SORTED = new ClientException("The list is not sorted");
}
