package ru.itmo.mit.cli;

import ru.itmo.mit.ExitException;
import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.guard.DefaultGuard;

import java.io.IOException;
import java.io.PrintStream;

public class LaunchBenchNumberClientsStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Server server;
    private final int fromNumberClients;
    private final int toNumberClients;
    private final int stepNumberClients;
    private final Client.Builder clientBuilder;

    public LaunchBenchNumberClientsStrategy(
            PrintStream printStream,
            Server server,
            int fromNumberClients,
            int toNumberClients,
            int stepNumberClients,
            Client.Builder clientBuilder
    ) {
        this.printStream = printStream;
        this.server = server;
        this.fromNumberClients = fromNumberClients;
        this.toNumberClients = toNumberClients;
        this.stepNumberClients = stepNumberClients;
        this.clientBuilder = clientBuilder;
    }

    @Override
    public StrategyCLI apply() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            for (int j = fromNumberClients; j <= toNumberClients; j = Integer.min(j + stepNumberClients, toNumberClients)) {
                Thread[] threadsClient = new Thread[j];
                var guard = new DefaultGuard(j);
                clientBuilder.setGuardSupplier(() -> guard);

                for (int i = 0; i < j; i++) {
                    var thread = new Thread(clientBuilder.build());
                    threadsClient[i] = thread;
                    thread.start();
                }
                for (int i = 0; i < j; i++) {
                    threadsClient[i].join();
                }
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
        printStream.println("Finish!");
        throw ExitException.INSTANCE;
    }
}
