package ru.itmo.mit.benchmarks;

import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.DefaultGuard;

import java.io.IOException;

public class BenchNumberClientsStrategy implements BenchmarkStrategy {
    private final Server server;
    private final int fromNumberClients;
    private final int toNumberClients;
    private final int stepNumberClients;
    private final Client.Builder clientBuilder;

    public BenchNumberClientsStrategy(
            Server server,
            int fromNumberClients,
            int toNumberClients,
            int stepNumberClients,
            Client.Builder clientBuilder
    ) {
        this.server = server;
        this.fromNumberClients = fromNumberClients;
        this.toNumberClients = toNumberClients;
        this.stepNumberClients = stepNumberClients;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public void launch() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            for (int j = fromNumberClients; j <= toNumberClients; j = Integer.min(j + stepNumberClients, toNumberClients)) {
                Thread[] threadsClient = new Thread[j];
                var guard = new DefaultGuard(j);
                clientBuilder.setGuardSupplier(() -> guard);

                BenchmarkStrategy.startAndJoinThreads(threadsClient, clientBuilder);
                if (j == toNumberClients) break;
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
    }
}
