package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;
import ru.mit.itmo.waiting.WaitingImpl;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;

import static ru.itmo.mit.Constants.NUMBER_SIMULTANEOUS_CONNECTIONS;
import static ru.itmo.mit.Constants.PROGRESS;

public class BenchDelayStrategy implements BenchmarkStrategy {
    private final FromToStep fromToStepDelay;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;
    private final LineChartSaver lineChartSaver;
    private final int numberWarmingIterations;

    public BenchDelayStrategy(
            FromToStep fromToStepDelay,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder,
            LineChartSaver lineChartSaver,
            int numberWarmingIterations
    ) {
        this.fromToStepDelay = fromToStepDelay;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
        this.lineChartSaver = lineChartSaver;
        this.numberWarmingIterations = numberWarmingIterations;
    }

    @Override
    public void launch(int port, PrintStream printStream) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(port);

        Thread[] threadsClient = new Thread[countClients];
        int from = fromToStepDelay.from();
        int to = fromToStepDelay.to();
        int step = fromToStepDelay.step();
        warmUp(from);
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            int delay = j;
            var guard = new GuardImpl(countClients, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(delay))).setGuardSupplier(() -> guard);
            printStream.printf(PROGRESS, j, to);
            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            if (!statisticsRecorder.isBroken()) lineChartSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            if (j == to) break;
        }
        lineChartSaver.save();
    }

    private void warmUp(int delay) throws InterruptedException {
        Thread[] threadsClient = new Thread[countClients];
        for (int i = 0; i < numberWarmingIterations; i++) {
            var guard = new GuardImpl(countClients, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(delay))).setGuardSupplier(() -> guard);
            BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
            statisticsRecorder.clear();
        }
    }
}