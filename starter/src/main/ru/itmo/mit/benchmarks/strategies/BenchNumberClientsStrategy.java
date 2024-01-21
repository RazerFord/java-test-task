package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.GraphSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.DefaultGuard;

import java.io.IOException;

public class BenchNumberClientsStrategy implements BenchmarkStrategy {
    private final Server server;
    private final int fromNumberClients;
    private final int toNumberClients;
    private final int stepNumberClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final GraphSaver graphSaver;

    public BenchNumberClientsStrategy(
            Server server,
            int fromNumberClients,
            int toNumberClients,
            int stepNumberClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            GraphSaver graphSaver
    ) {
        this.server = server;
        this.fromNumberClients = fromNumberClients;
        this.toNumberClients = toNumberClients;
        this.stepNumberClients = stepNumberClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.graphSaver = graphSaver;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            for (int j = fromNumberClients; j <= toNumberClients; j = Integer.min(j + stepNumberClients, toNumberClients)) {
                statisticsRecorder.updateValue(j);
                Thread[] threadsClient = new Thread[j];
                var guard = new DefaultGuard(j);
                clientBuilder.setGuardSupplier(() -> guard);

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
                graphSaver.append(statisticsRecorder);
                statisticsRecorder.clear();
                if (j == toNumberClients) break;
            }
            graphSaver.save();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException ignored) {
            // this code block is empty
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
