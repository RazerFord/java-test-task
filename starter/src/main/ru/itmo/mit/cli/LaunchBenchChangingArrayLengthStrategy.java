package ru.itmo.mit.cli;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.ExitException;
import ru.itmo.mit.Server;

import java.io.IOException;
import java.io.PrintStream;

public class LaunchBenchChangingArrayLengthStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Server server;
    private final int countRequests;
    private final int fromArrayLength;
    private final int toArrayLength;
    private final int stepArrayLength;
    private final int countClients;
    private final int delay;

    private LaunchBenchChangingArrayLengthStrategy(
            PrintStream printStream,
            Server server,
            int countRequests,
            int fromArrayLength,
            int toArrayLength,
            int stepArrayLength,
            int countClients,
            int delay
    ) {
        this.printStream = printStream;
        this.server = server;
        this.countRequests = countRequests;
        this.fromArrayLength = fromArrayLength;
        this.toArrayLength = toArrayLength;
        this.stepArrayLength = stepArrayLength;
        this.countClients = countClients;
        this.delay = delay;
    }

    @Override
    public StrategyCLI apply() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();

            Thread[] threadsClient = new Thread[countClients];
            for (int i = 0; i < countClients; i++) {
                var thread = new Thread();
                threadsClient[i] = thread;
                thread.start();
            }
            for (int i = 0; i < countClients; i++) {
                threadsClient[i].join();
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

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PrintStream printStream;
        private Server server;
        private int countRequests;
        private int fromArrayLength;
        private int toArrayLength;
        private int stepArrayLength;
        private int countClients;
        private int delay;

        private Builder() {
        }

        public Builder setPrintStream(PrintStream printStream) {
            this.printStream = printStream;
            return this;
        }

        public Builder setServer(Server server) {
            this.server = server;
            return this;
        }

        public Builder setCountRequests(int countRequests) {
            this.countRequests = countRequests;
            return this;
        }

        public Builder setFromArrayLength(int fromArrayLength) {
            this.fromArrayLength = fromArrayLength;
            return this;
        }

        public Builder setToArrayLength(int toArrayLength) {
            this.toArrayLength = toArrayLength;
            return this;
        }

        public Builder setStepArrayLength(int stepArrayLength) {
            this.stepArrayLength = stepArrayLength;
            return this;
        }

        public Builder setCountClients(int countClients) {
            this.countClients = countClients;
            return this;
        }

        public Builder setDelay(int delay) {
            this.delay = delay;
            return this;
        }

        public LaunchBenchChangingArrayLengthStrategy build() {
            return new LaunchBenchChangingArrayLengthStrategy(
                    printStream,
                    server,
                    countRequests,
                    fromArrayLength,
                    toArrayLength,
                    stepArrayLength,
                    countClients,
                    delay
            );
        }
    }
}
