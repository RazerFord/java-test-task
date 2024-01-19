package ru.itmo.mit;

import ru.mit.itmo.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.function.Supplier;

public class LaunchingStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final Server server;
    private final int countClients;
    private final Supplier<Client> clientSupplier;

    public LaunchingStrategy(
            PrintStream printStream,
            Scanner scanner,
            Server server,
            int countClients,
            Supplier<Client> clientSupplier
    ) {
        this.printStream = printStream;
        this.scanner = scanner;
        this.server = server;
        this.countClients = countClients;
        this.clientSupplier = clientSupplier;
    }

    @Override
    public StrategyCLI apply() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int i = 0; i < countClients; i++) {
                var thread = new Thread(clientSupplier.get());
                threadsClient[i] = thread;
                thread.start();
            }
            for (int i = 0; i < countClients; i++) {
                threadsClient[i].join();
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                server.close();
            } catch (IOException ignored) {
                // is the block empty on purpose
            }
        }
        throw ExitException.INSTANCE;
    }
}
