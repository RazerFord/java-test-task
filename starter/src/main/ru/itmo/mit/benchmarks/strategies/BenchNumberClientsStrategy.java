package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.GraphSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.DefaultGuard;

import java.io.IOException;

public class BenchNumberClientsStrategy implements BenchmarkStrategy {
    private final Server server;
    private final FromToStep fromToStepClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final GraphSaver graphSaver;

    public BenchNumberClientsStrategy(
            Server server,
            FromToStep fromToStepClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            GraphSaver graphSaver
    ) {
        this.server = server;
        this.fromToStepClients = fromToStepClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.graphSaver = graphSaver;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            int from = fromToStepClients.from();
            int to = fromToStepClients.to();
            int step = fromToStepClients.step();
            for (int j = from; j <= to; j = Integer.min(j + step, to)) {
                statisticsRecorder.updateValue(j);
                Thread[] threadsClient = new Thread[j];
                var guard = new DefaultGuard(j);
                clientBuilder.setGuardSupplier(() -> guard);

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
                graphSaver.append(statisticsRecorder);
                statisticsRecorder.clear();
                if (j == to) break;
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
