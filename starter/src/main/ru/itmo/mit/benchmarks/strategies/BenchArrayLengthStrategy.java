package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
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
    private final StatisticsRecorder statisticsRecorder;

    public BenchArrayLengthStrategy(
            Server server,
            int fromArrayLength,
            int toArrayLength,
            int stepArrayLength,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder
    ) {
        this.server = server;
        this.fromArrayLength = fromArrayLength;
        this.toArrayLength = toArrayLength;
        this.stepArrayLength = stepArrayLength;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int j = fromArrayLength; j <= toArrayLength; j = Integer.min(j + stepArrayLength, toArrayLength)) {
                statisticsRecorder.updateValue(j);
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
