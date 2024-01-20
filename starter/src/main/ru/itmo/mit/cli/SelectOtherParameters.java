package ru.itmo.mit.cli;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;
import ru.mit.itmo.guard.DefaultGuard;
import ru.mit.itmo.waiting.DefaultWaiting;

import java.io.PrintStream;
import java.time.Duration;
import java.util.Scanner;

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
        int from = scanner.nextInt();
        int to = scanner.nextInt();
        int step = scanner.nextInt();
        if (from < 0 || to < 0 || from > to || step <= 0)
            throw new IllegalArgumentException("Should be: from >= 0, to >= 0, step > 0, from <= step");

        printStream.printf(Constants.SELECT_OTHER_VALUES, removeElement(Constants.PARAMS, numberParam - 1));
        int other1 = scanner.nextInt();
        if (other1 < 0) throw Constants.PARAMETER_NOT_NEGATIVE;
        int other2 = scanner.nextInt();
        if (other2 < 0) throw Constants.PARAMETER_NOT_NEGATIVE;

        var builderClient = Client.builder()
                .setTargetAddress(Constants.ADDRESS)
                .setTargetPort(Constants.PORT)
                .setCountRequest(countRequests);

        return switch (numberParam) {
            case 1 -> {
                var guard = new DefaultGuard(other1);

                builderClient
                        .setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(other2)))
                        .setGuardSupplier(() -> guard);

                yield new LaunchBenchArrayLengthStrategy(printStream, server, from, to, step, other1, builderClient);
            }
            case 2 -> {
                builderClient
                        .setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(other1))
                        .setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(other2)));

                yield new LaunchBenchNumberClientsStrategy(printStream, server, from, to, step, builderClient);
            }

            case 3 -> {
                var guard = new DefaultGuard(other2);

                builderClient
                        .setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(other1))
                        .setGuardSupplier(() -> guard);

                yield new LaunchBenchDelayStrategy(printStream, server, from, to, step, other2, builderClient);
            }

            default -> throw new IllegalArgumentException(SELECTION_ERROR);
        };
    }

    @Contract(pure = true)
    private Object @NotNull [] removeElement(Object @NotNull [] array, int index) {
        Object[] newArray = new Object[array.length - 1];
        for (int i = 0, j = 0; i < array.length; i++) {
            if (i == index) continue;
            newArray[j++] = array[i];
        }
        return newArray;
    }

    private static final String SELECTION_ERROR = "The parameter number is incorrect";
}
