package ru.itmo.mit.benchmarks.strategies;

import org.jetbrains.annotations.NotNull;
import ru.mit.itmo.Client;

import java.io.IOException;
import java.io.PrintStream;

public interface BenchmarkStrategy {
    void launch(int port, PrintStream printStream) throws InterruptedException, IOException;

    static void startAndJoinThreads(Thread @NotNull [] threadsClient, Client.Builder clientBuilder) throws InterruptedException {
        int length = threadsClient.length;
        for (int i = 0; i < length; i++) {
            var thread = new Thread(clientBuilder.build());
            threadsClient[i] = thread;
            thread.start();
        }
        for (Thread thread : threadsClient) {
            thread.join();
        }
    }
}
