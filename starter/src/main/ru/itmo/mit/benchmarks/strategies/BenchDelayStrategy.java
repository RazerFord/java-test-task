package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.GraphicsSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;
import ru.mit.itmo.waiting.WaitingImpl;

import java.io.IOException;
import java.time.Duration;

public class BenchDelayStrategy implements BenchmarkStrategy {
    private final Server server;
    private final FromToStep fromToStepDelay;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final GraphicsSaver graphicsSaver;

    public BenchDelayStrategy(
            Server server,
            FromToStep fromToStepDelay,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            GraphicsSaver graphicsSaver
    ) {
        this.server = server;
        this.fromToStepDelay = fromToStepDelay;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.graphicsSaver = graphicsSaver;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();
            clientBuilder.setTargetPort(server.getPort());

            Thread[] threadsClient = new Thread[countClients];
            int from = fromToStepDelay.from();
            int to = fromToStepDelay.to();
            int step = fromToStepDelay.step();
            for (int j = from; j <= to; j = Integer.min(j + step, to)) {
                statisticsRecorder.updateValue(j);
                int delay = j;
                var guard = new GuardImpl(countClients);
                clientBuilder.setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(delay)))
                        .setGuardSupplier(() -> guard);

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
                if (!statisticsRecorder.isBroken()) graphicsSaver.append(statisticsRecorder);
                statisticsRecorder.clear();
                if (j == to) break;
            }
            graphicsSaver.save();
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
