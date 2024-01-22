package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;

import java.io.IOException;

import static ru.itmo.mit.Constants.NUMBER_SIMULTANEOUS_CONNECTIONS;

public class BenchNumberClientsStrategy implements BenchmarkStrategy {
    private final FromToStep fromToStepClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final LineChartSaver lineChartSaver;
    private final int numberWarmingIterations;

    public BenchNumberClientsStrategy(
            FromToStep fromToStepClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            LineChartSaver lineChartSaver,
            int numberWarmingIterations
    ) {
        this.fromToStepClients = fromToStepClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.lineChartSaver = lineChartSaver;
        this.numberWarmingIterations = numberWarmingIterations;
    }

    @Override
    public void launch(int port) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(port);

        int from = fromToStepClients.from();
        int to = fromToStepClients.to();
        int step = fromToStepClients.step();
        warmUp(from);
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            Thread[] threadsClient = new Thread[j];
            var guard = new GuardImpl(j, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setGuardSupplier(() -> guard);

            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            if (!statisticsRecorder.isBroken()) lineChartSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            if (j == to) break;
        }
        lineChartSaver.save();
    }

    private void warmUp(int countClients) throws InterruptedException {
        Thread[] threadsClient = new Thread[countClients];
        for (int i = 0; i < numberWarmingIterations; i++){
            var guard = new GuardImpl(countClients, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setGuardSupplier(() -> guard);
            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
        }
    }
}
