package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.Client;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

public interface BenchmarkStrategy {
    void launch(Server server, PrintStream printStream) throws InterruptedException, IOException;

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

    class ClientLauncher {
        private final Client.Builder clientBuilder;
        private final Thread[] threadsClient;
        private final Client[] clients;

        public ClientLauncher(int numberClients, Client.Builder clientBuilder) {
            this.clientBuilder = clientBuilder;
            threadsClient = new Thread[numberClients];
            clients = new Client[numberClients];
        }

        public void launch() throws InterruptedException {
            int length = threadsClient.length;
            for (int i = 0; i < length; i++) {
                var client = clientBuilder.build();
                clients[i] = client;
                var thread = new Thread(client);
                threadsClient[i] = thread;
                thread.start();
            }
            for (Thread thread : threadsClient) {
                thread.join();
            }
        }

        public Client[] getClients() {
            return clients;
        }
    }
}
