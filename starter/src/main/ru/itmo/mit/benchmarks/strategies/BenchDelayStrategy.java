package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.Client;
import ru.mit.itmo.waiting.DefaultWaiting;

import java.io.IOException;
import java.time.Duration;

public class BenchDelayStrategy implements BenchmarkStrategy {
    private final Server server;
    private final int fromDelay;
    private final int toDelay;
    private final int stepDelay;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;

    public BenchDelayStrategy(
            Server server,
            int fromDelay,
            int toDelay,
            int stepDelay,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder
    ) {
        this.server = server;
        this.fromDelay = fromDelay;
        this.toDelay = toDelay;
        this.stepDelay = stepDelay;
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
            for (int j = fromDelay; j <= toDelay; j = Integer.min(j + stepDelay, toDelay)) {
                statisticsRecorder.updateValue(j);
                int delay = j;
                clientBuilder.setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(delay)));

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
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
    }
}
