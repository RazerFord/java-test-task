package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BenchArrayLengthStrategy implements BenchmarkStrategy {
    private final Server server;
    private final int fromArrayLength;
    private final int toArrayLength;
    private final int stepArrayLength;
    private final int countClients;
    private final Client.Builder clientBuilder;
    private final StatisticsRecorder statisticsRecorder;

    public BenchArrayLengthStrategy(
            Server server,
            int fromArrayLength,
            int toArrayLength,
            int stepArrayLength,
            int countClients,
            Client.Builder clientBuilder,
            StatisticsRecorder statisticsRecorder
    ) {
        this.server = server;
        this.fromArrayLength = fromArrayLength;
        this.toArrayLength = toArrayLength;
        this.stepArrayLength = stepArrayLength;
        this.countClients = countClients;
        this.clientBuilder = clientBuilder;
        this.statisticsRecorder = statisticsRecorder;
    }

    @Override
    public void launch() {
        List<Integer> values = new ArrayList<>();
        List<Integer> processingRequest = new ArrayList<>();
        List<Integer> processingClient = new ArrayList<>();
        List<Integer> averageRequestProcessingTime = new ArrayList<>();
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int j = fromArrayLength; j <= toArrayLength; j = Integer.min(j + stepArrayLength, toArrayLength)) {
                statisticsRecorder.updateValue(j);
                int arrayLength = j;
                clientBuilder.setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(arrayLength));

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
                values.add(j);
                processingRequest.add((int) statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_REQUEST));
                processingClient.add((int) statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_CLIENT));
                averageRequestProcessingTime.add((int) statisticsRecorder.average(StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME));
                statisticsRecorder.clear();
                if (j == toArrayLength) break;
            }
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException ignored) {
                // is the block empty on purpose
            }
        }
        try {
            FileWriter fos1;
            FileWriter fos2;
            FileWriter fos3;
            try {
                fos1 = new FileWriter("processingRequest.txt");
                fos2 = new FileWriter("processingClient.txt");
                fos3 = new FileWriter("averageRequestProcessingTime.txt");
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            for (int i = 0; i < values.size(); i++) {
                int value = values.get(i);
                fos1.write(String.valueOf(value));
                fos1.write(" ");
                fos1.write(String.valueOf(processingRequest.get(i)));
                fos1.write("\n");
                fos2.write(String.valueOf(value));
                fos2.write(" ");
                fos2.write(String.valueOf(processingClient.get(i)));
                fos2.write("\n");
                fos3.write(String.valueOf(value));
                fos3.write(" ");
                fos3.write(String.valueOf(averageRequestProcessingTime.get(i)));
                fos3.write("\n");
            }
            fos1.flush();
            fos2.flush();
            fos3.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
