package ru.itmo.mit.cli;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;

import java.io.PrintStream;
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
        int step = scanner.nextInt();
        int from = scanner.nextInt();
        int to = scanner.nextInt();
        if (from < 0 || to < 0 || from > to || step <= 0) {
            throw new IllegalArgumentException("Should be: from >= 0, to >= 0, step > 0, from <= step");
        }
        printStream.printf(Constants.SELECT_OTHER_VALUES, removeElement(Constants.PARAMS, numberParam - 1));
        int other1 = scanner.nextInt();
        if (other1 < 0) throw Constants.PARAMETER_NOT_NEGATIVE;
        int other2 = scanner.nextInt();
        if (other2 < 0) throw Constants.PARAMETER_NOT_NEGATIVE;

        return switch (numberParam) {
            case 1 -> LaunchBenchChangingArrayLengthStrategy.builder()
                    .setPrintStream(printStream)
                    .setServer(server)
                    .setCountRequests(countRequests)
                    .setFromArrayLength(from)
                    .setToArrayLength(to)
                    .setStepArrayLength(step)
                    .setCountClients(other1)
                    .setDelay(other2)
                    .build();

            case 2 -> null;

            case 3 -> null;

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
