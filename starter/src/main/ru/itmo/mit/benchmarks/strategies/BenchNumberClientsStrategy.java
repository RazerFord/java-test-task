package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.FromToStep;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.GuardImpl;

import java.io.IOException;
import java.io.PrintStream;

import static ru.itmo.mit.Constants.NUMBER_SIMULTANEOUS_CONNECTIONS;
import static ru.itmo.mit.Constants.PROGRESS;

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
    public void launch(Server server, PrintStream printStream) throws InterruptedException, IOException {
        clientBuilder.setTargetPort(server.getPort());

        int from = fromToStepClients.from();
        int to = fromToStepClients.to();
        int step = fromToStepClients.step();
        warmUp(from);
        for (int j = from; j <= to; j = Integer.min(j + step, to)) {
            statisticsRecorder.updateValue(j);
            var guard = new GuardImpl(j, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setGuardSupplier(() -> guard);
            printStream.printf(PROGRESS, j, to);
            var clientLauncher = new ClientLauncher(j, clientBuilder);
            clientLauncher.launch();
            if (!statisticsRecorder.isBroken()) lineChartSaver.append(statisticsRecorder);
            statisticsRecorder.clear();
            server.reset();
            if (j == to) break;
        }
        lineChartSaver.save();
    }

    private void warmUp(int countClients) throws InterruptedException {
        for (int i = 0; i < numberWarmingIterations; i++){
            var guard = new GuardImpl(countClients, NUMBER_SIMULTANEOUS_CONNECTIONS);
            clientBuilder.setGuardSupplier(() -> guard);
            var clientLauncher = new ClientLauncher(countClients, clientBuilder);
            clientLauncher.launch();
        }
    }
}
