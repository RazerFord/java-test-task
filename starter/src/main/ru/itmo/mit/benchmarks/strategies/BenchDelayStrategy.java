package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;
import ru.mit.itmo.waiting.WaitingImpl;

import java.io.IOException;
import java.time.Duration;

import static ru.itmo.mit.Constants.NUMBER_SIMULTANEOUS_CONNECTIONS;

public class BenchDelayStrategy implements BenchmarkStrategy {
    private final FromToStep fromToStepDelay;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final LineChartSaver lineChartSaver;

    public BenchDelayStrategy(
            FromToStep fromToStepDelay,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            LineChartSaver lineChartSaver
    ) {
        this.fromToStepDelay = fromToStepDelay;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.lineChartSaver = lineChartSaver;
    }

    @Override
    public void launch(int port) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(port);

        Thread[] threadsClient = new Thread[countClients];
        int from = fromToStepDelay.from();
        int to = fromToStepDelay.to();
        int step = fromToStepDelay.step();
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            int delay = j;
            var guard = new GuardImpl(countClients, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(delay)))
                    .setGuardSupplier(() -> guard);

            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            if (!statisticsRecorder.isBroken()) lineChartSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            if (j == to) break;
        }
        lineChartSaver.save();
    }
}