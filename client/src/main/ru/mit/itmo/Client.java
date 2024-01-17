package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.mit.itmo.arraygenerators.ArrayGenerators;

import java.io.IOException;
import java.net.Socket;
import java.time.Duration;
import java.util.List;
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
    private final Duration periodRequest;
    private Duration lastRequestTime = Duration.ZERO;

    public Client(
            String targetAddress,
            int targetPort,
            ArrayGenerators arrayGenerators,
            int countRequest,
            int periodRequest // ms
    ) {
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
        this.arrayGenerators = arrayGenerators;
        this.countRequest = countRequest;
        this.periodRequest = Duration.ofMillis(periodRequest);
    }

    @Override
    public void run() {
        try (
                var socket = new Socket(targetAddress, targetPort);
                var outputStream = socket.getOutputStream();
                var inputStream = socket.getInputStream()
        ) {
            for (int i = 0; i < countRequest; i++) {
                trySleep();
                var message = buildRequest();
                messageSender.send(message, outputStream);
                message = messageReader.read(inputStream);
                lastRequestTime = Duration.ofMillis(System.currentTimeMillis());
                checkSortingList(message.getNumberList());
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
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

    private void trySleep() throws InterruptedException {
        Duration currTime = Duration.ofMillis(System.currentTimeMillis());
        Duration endSleepTime = lastRequestTime.plus(periodRequest);
        Duration diff = endSleepTime.minus(currTime);
        while (diff.isPositive()) {
            Thread.sleep(diff);
            currTime = Duration.ofMillis(System.currentTimeMillis());
            diff = endSleepTime.minus(currTime);
        }
    }

    private static final ClientException LIST_NOT_SORTED = new ClientException("The list is not sorted");
}
