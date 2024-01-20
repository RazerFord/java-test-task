package ru.itmo.mit.cli;

import ru.itmo.mit.ExitException;
import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.waiting.DefaultWaiting;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;

public class LaunchBenchDelayStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Server server;
    private final int fromDelay;
    private final int toDelay;
    private final int stepDelay;
    private final int countClients;
    private final Client.Builder clientBuilder;

    public LaunchBenchDelayStrategy(
            PrintStream printStream,
            Server server,
            int fromDelay,
            int toDelay,
            int stepDelay,
            int countClients,
            Client.Builder clientBuilder
    ) {
        this.printStream = printStream;
        this.server = server;
        this.fromDelay = fromDelay;
        this.toDelay = toDelay;
        this.stepDelay = stepDelay;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public StrategyCLI apply() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int j = fromDelay; j <= toDelay; j = Integer.min(j + stepDelay, toDelay)) {
                int delay = j;
                clientBuilder.setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(delay)));

                for (int i = 0; i < countClients; i++) {
                    var thread = new Thread(clientBuilder.build());
                    threadsClient[i] = thread;
                    thread.start();
                }
                for (int i = 0; i < countClients; i++) {
                    threadsClient[i].join();
                }
                if (j == toDelay) break;
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
