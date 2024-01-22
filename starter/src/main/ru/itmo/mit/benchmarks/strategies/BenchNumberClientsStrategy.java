package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.GraphicsSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;

import java.io.IOException;

public class BenchNumberClientsStrategy implements BenchmarkStrategy {
    private final FromToStep fromToStepClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final GraphicsSaver graphicsSaver;

    public BenchNumberClientsStrategy(
            FromToStep fromToStepClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            GraphicsSaver graphicsSaver
    ) {
        this.fromToStepClients = fromToStepClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.graphicsSaver = graphicsSaver;
    }

    @Override
    public void launch(int port) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(port);

        int from = fromToStepClients.from();
        int to = fromToStepClients.to();
        int step = fromToStepClients.step();
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            Thread[] threadsClient = new Thread[j];
            var guard = new GuardImpl(j);
            clientBuilder.setGuardSupplier(() -> guard);

            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            if (!statisticsRecorder.isBroken()) graphicsSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            if (j == to) break;
        }
        graphicsSaver.save();
    }
}
