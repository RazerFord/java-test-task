package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.ArrayGenerators;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;
import ru.mit.itmo.arraygenerators.IncreasingArrayGenerators;
import ru.mit.itmo.waiting.DefaultWaiting;
import ru.mit.itmo.waiting.IncreasingWaiting;
import ru.mit.itmo.waiting.Waiting;

import java.time.Duration;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;

public class CLI {
    private static final String ADDRESS = "0.0.0.0";
    private static final int PORT = 8081;

    public void start() throws Exception {
        Scanner reader = new Scanner(System.in);
        final Set<String> params = Set.of("N", "M", "Δ");

        System.out.print(SELECT_ARCHITECTURE);
        int serverNumber = reader.nextInt();

        System.out.print(SELECT_ARCHITECTURE);
        int countRequests = reader.nextInt();

        System.out.print(SELECT_CHANGEABLE_PARAM);
        int numberParam = reader.nextInt();

        System.out.print(SELECT_STEP_FROM_TO);
        int step = reader.nextInt(), from = reader.nextInt(), to = reader.nextInt();

        System.out.printf(SELECT_OTHER_VALUES, params.toArray());
        int other1 = reader.nextInt();
        int other2 = reader.nextInt();

        var clientSupplier = createClientSupplier(numberParam, step, from, to, other1, other2, countRequests);

        try (var ignored = this.createServer(serverNumber)) {
            Thread[] threads = new Thread[other1];
            for (int i = 0; i < other1; i++) {
                threads[i] = new Thread(clientSupplier.get());
            }
            for (int i = 0; i < other1; i++) {
                threads[i].join();
            }
        }
    }

    private Server createServer(int i) {
        return switch (i) {
            case 1 -> new BlockingServer(PORT);
            case 2 -> new NonBlockingServer(PORT);
            case 3 -> new AsyncServer(PORT);
            default -> throw new IllegalArgumentException();
        };
    }

    private @NotNull Supplier<Client> createClientSupplier(
            int numberParam,
            int step,
            int from,
            int to,
            int other1,
            int other2,
            int countRequests
    ) {
        ArrayGenerators generator;
        Waiting waiting;
        switch (numberParam) {
            case 1 -> {
                generator = new IncreasingArrayGenerators(from, to, step);
                waiting = new DefaultWaiting(Duration.ofMillis(other2));
            }
            case 2 -> {
                generator = new DefaultArrayGenerators(other1);
                waiting = new DefaultWaiting(Duration.ofMillis(other2));
            }
            case 3 -> {
                generator = new DefaultArrayGenerators(other1);
                waiting = new IncreasingWaiting(Duration.ofMillis(from), Duration.ofMillis(to), Duration.ofMillis(step));
            }
            default -> throw new IllegalArgumentException();
        }
        return () -> new Client(ADDRESS, PORT, generator, countRequests, waiting);
    }

    private static final String SELECT_ARCHITECTURE = """
            Select architecture:
            1. Blocking
            2. Non-blocking
            3. Asynchronous
            """;

    private static final String SELECT_NUMBER_REQUESTS = """
            Select number of requests:
            """;

    private static final String SELECT_CHANGEABLE_PARAM = """
            Select parameter to change:
            1. N - Number of elements in the array
            2. M - Number of working clients
            3. Δ - Time interval between requests
            """;

    private static final String SELECT_STEP_FROM_TO = """
            Select step, from and to
            """;

    private static final String SELECT_OTHER_VALUES = """
            Select other values: %s, %s
            N - Number of elements in the array
            M - Number of working clients
            Δ - Time interval between requests
            """;
}
