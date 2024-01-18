package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;
import ru.mit.itmo.arraygenerators.ArrayGenerators;
import ru.mit.itmo.waiting.Waiting;

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
    private final Waiting waiting;

    public Client(
            String targetAddress,
            int targetPort,
            ArrayGenerators arrayGenerators,
            int countRequest,
            Waiting waiting
    ) {
        this.targetAddress = targetAddress;
        this.targetPort = targetPort;
        this.arrayGenerators = arrayGenerators;
        this.countRequest = countRequest;
        this.waiting = waiting;
    }

    @Override
    public void run() {
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

    private static final ClientException LIST_NOT_SORTED = new ClientException("The list is not sorted");
}
