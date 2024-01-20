package ru.itmo.mit.cli;

import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.ArrayGenerators;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;
import ru.mit.itmo.arraygenerators.IncreasingArrayGenerators;
import ru.mit.itmo.guard.DefaultGuard;
import ru.mit.itmo.guard.Guard;
import ru.mit.itmo.guard.StrictGuard;
import ru.mit.itmo.waiting.DefaultWaiting;
import ru.mit.itmo.waiting.IncreasingWaiting;
import ru.mit.itmo.waiting.Waiting;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Scanner;
import java.util.function.Supplier;

public class SelectOtherParameters implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final Server server;
    private final int countRequests;

    public SelectOtherParameters(
            PrintStream printStream,
            Scanner scanner,
            Server server,
            int countRequests
    ) {
        this.printStream = printStream;
        this.scanner = scanner;
        this.server = server;
        this.countRequests = countRequests;
    }

    @Override
    public StrategyCLI apply() {
        printStream.print(Constants.SELECT_CHANGEABLE_PARAM);
        int numberParam = scanner.nextInt();

        printStream.print(Constants.SELECT_STEP_FROM_TO);
        int step = scanner.nextInt();
        int from = scanner.nextInt();
        int to = scanner.nextInt();

        printStream.printf(Constants.SELECT_OTHER_VALUES, Constants.PARAMS.toArray());
        int other1 = scanner.nextInt();
        int other2 = scanner.nextInt();

        int countClients;
        Supplier<ArrayGenerators> generatorsSupplier;
        Supplier<Waiting> waitingSupplier;
        Supplier<Guard> guardSupplier;
        switch (numberParam) {
            case 1 -> {
                countClients = other1;
                generatorsSupplier = () -> new IncreasingArrayGenerators(from, to, step);
                guardSupplier = DefaultGuard::new;
                waitingSupplier = () -> new DefaultWaiting(Duration.ofMillis(other2));
            }
            case 2 -> {
                countClients = to;
                generatorsSupplier = () -> new DefaultArrayGenerators(other1);
                guardSupplier = () -> new StrictGuard(from, to, step);
                waitingSupplier = () -> new DefaultWaiting(Duration.ofMillis(other2));
            }
            case 3 -> {
                countClients = other2;
                generatorsSupplier = () -> new DefaultArrayGenerators(other1);
                guardSupplier = DefaultGuard::new;
                waitingSupplier = () -> new IncreasingWaiting(Duration.ofMillis(from), Duration.ofMillis(to), Duration.ofMillis(step));
            }
            default -> throw new IllegalArgumentException(SELECTION_ERROR);
        }
        Supplier<Client> clientSupplier = () -> {
            var generator = generatorsSupplier.get();
            var waiting = waitingSupplier.get();
            var guard = guardSupplier.get();
            return new Client(Constants.ADDRESS, Constants.PORT, countRequests, generator, waiting, guard);
        };
        return new LaunchingStrategy(printStream, server, countClients, clientSupplier);
    }

    private static final String SELECTION_ERROR = "The parameter number is incorrect";
}
