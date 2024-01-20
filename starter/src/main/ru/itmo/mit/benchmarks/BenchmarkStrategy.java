package ru.itmo.mit.benchmarks;

import org.jetbrains.annotations.NotNull;
import ru.mit.itmo.Client;

public interface BenchmarkStrategy {
    void launch();

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
