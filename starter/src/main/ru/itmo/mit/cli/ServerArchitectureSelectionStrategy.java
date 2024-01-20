package ru.itmo.mit.cli;

import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;
import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;

import java.io.PrintStream;
import java.util.Scanner;

public class ServerArchitectureSelectionStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;

    public ServerArchitectureSelectionStrategy(PrintStream printStream, Scanner scanner) {
        this.printStream = printStream;
        this.scanner = scanner;
    }

    @Override
    public StrategyCLI apply() {
        printStream.println(Constants.SELECT_ARCHITECTURE);
        var server = createServer(scanner.nextInt());
        return new SelectingNumberRequestsStrategy(printStream, scanner, server);
    }

    private Server createServer(int architectureNumber) {
        return switch (architectureNumber) {
            case 1 -> new BlockingServer(Constants.PORT);
            case 2 -> new NonBlockingServer(Constants.PORT);
            case 3 -> new AsyncServer(Constants.PORT);
            default -> throw new IllegalArgumentException(SELECTION_ERROR);
        };
    }

    private static final String SELECTION_ERROR = "There is no architecture with this number";
}
