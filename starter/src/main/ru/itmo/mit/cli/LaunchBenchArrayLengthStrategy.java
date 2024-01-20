package ru.itmo.mit.cli;

import ru.itmo.mit.ExitException;
import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;

import java.io.IOException;
import java.io.PrintStream;

public class LaunchBenchArrayLengthStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Server server;
    private final int fromArrayLength;
    private final int toArrayLength;
    private final int stepArrayLength;
    private final int countClients;
    private final Client.Builder clientBuilder;

    public LaunchBenchArrayLengthStrategy(
            PrintStream printStream,
            Server server,
            int fromArrayLength,
            int toArrayLength,
            int stepArrayLength,
            int countClients,
            Client.Builder clientBuilder
    ) {
        this.printStream = printStream;
        this.server = server;
        this.fromArrayLength = fromArrayLength;
        this.toArrayLength = toArrayLength;
        this.stepArrayLength = stepArrayLength;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public StrategyCLI apply() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int j = fromArrayLength; j <= toArrayLength; j = Integer.min(j + stepArrayLength, toArrayLength)) {
                int arrayLength = j;
                clientBuilder.setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(arrayLength));

                for (int i = 0; i < countClients; i++) {
                    var thread = new Thread(clientBuilder.build());
                    threadsClient[i] = thread;
                    thread.start();
                }
                for (int i = 0; i < countClients; i++) {
                    threadsClient[i].join();
                }
                if (j == toArrayLength) break;
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException ignored) {
                // is the block empty on purpose
            }
        }
        printStream.println("Finish!");
        throw ExitException.INSTANCE;
    }
}
