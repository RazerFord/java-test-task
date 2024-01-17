package ru.mit.itmo;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.MessageOuterClass;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(Client.class.getName());
    private final Random random = new Random();
    private final String targetAddress;
    private final int targetPort;
    private final int countNumbers;
    private final int countRequest;
    private final int periodRequest;
    private final MessageReader messageReader = new MessageReader();
    private final MessageSender messageSender = new MessageSender();

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
                messageSender.send(message, outputStream);
                message = messageReader.read(inputStream);
                checkSortingList(message.getNumberList());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, e.getMessage());
        }
    }

    private MessageOuterClass.@NotNull Message buildRequest() {
        List<Integer> numbers = new ArrayList<>(countNumbers);
        for (int i = 0; i < countNumbers; i++) {
            numbers.add(random.nextInt());
        }
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
