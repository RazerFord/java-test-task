package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;

import java.io.IOException;

public class BenchArrayLengthStrategy implements BenchmarkStrategy {
    private final Server server;
    private final int fromArrayLength;
    private final int toArrayLength;
    private final int stepArrayLength;
    private final int countClients;
    private final Client.Builder clientBuilder;

    public BenchArrayLengthStrategy(
            Server server,
            int fromArrayLength,
            int toArrayLength,
            int stepArrayLength,
            int countClients,
            Client.Builder clientBuilder
    ) {
        this.server = server;
        this.fromArrayLength = fromArrayLength;
        this.toArrayLength = toArrayLength;
        this.stepArrayLength = stepArrayLength;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int j = fromArrayLength; j <= toArrayLength; j = Integer.min(j + stepArrayLength, toArrayLength)) {
                int arrayLength = j;
                clientBuilder.setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(arrayLength));

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
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
    }
}
