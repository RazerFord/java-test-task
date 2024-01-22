package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.GraphicsSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.ArrayGeneratorsImpl;
import ru.mit.itmo.guard.GuardImpl;

import java.io.IOException;

public class BenchArrayLengthStrategy implements BenchmarkStrategy {
    private final FromToStep fromToStepLength;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final GraphicsSaver graphicsSaver;

    public BenchArrayLengthStrategy(
            FromToStep fromToStepLength,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            GraphicsSaver graphicsSaver
    ) {
        this.fromToStepLength = fromToStepLength;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.graphicsSaver = graphicsSaver;
    }

    @Override
    public void launch(int port) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(port);
        Thread[] threadsClient = new Thread[countClients];
        int from = fromToStepLength.from();
        int to = fromToStepLength.to();
        int step = fromToStepLength.step();
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            int arrayLength = j;
            var guard = new GuardImpl(countClients);
            clientBuilder.setArrayGeneratorsSupplier(() -> new ArrayGeneratorsImpl(arrayLength))
                    .setGuardSupplier(() -> guard);
            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            if (!statisticsRecorder.isBroken()) graphicsSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            if (j == to) break;
        }
        graphicsSaver.save();
    }
}
