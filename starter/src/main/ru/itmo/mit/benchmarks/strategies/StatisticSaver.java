package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.Client;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

class StatisticSaver {
    private final Server server;
    private final StatisticsRecorder statisticsRecorder;
    private final LineChartSaver lineChartSaver;

    public StatisticSaver(
            Server server,
            StatisticsRecorder statisticsRecorder,
            LineChartSaver lineChartSaver
    ) {
        this.server = server;
        this.statisticsRecorder = statisticsRecorder;
        this.lineChartSaver = lineChartSaver;
    }

    void startAndSaveStatistic(
            int countClients,
            Client.Builder clientBuilder,
            int value
    ) throws InterruptedException {
        var clientLauncher = new ClientLauncher(countClients, clientBuilder);
        clientLauncher.launch();
        if (!statisticsRecorder.isBroken()) {
            Queue<Long> queue = new ArrayDeque<>();
            Arrays.stream(clientLauncher.getClients()).forEach(it -> it.addIfNonZeroAverageRequestTime(queue));
            var avgRequestOnClient = statisticsRecorder.average(queue);
            var clientProcessingOnServer = server.getClientProcessingTime();
            var requestProcessingOnServer = server.getRequestProcessingTime();
            lineChartSaver.append(
                    value,
                    requestProcessingOnServer,
                    clientProcessingOnServer,
                    avgRequestOnClient
            );
        }
    }
}