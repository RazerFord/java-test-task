package ru.itmo.mit.cli;

import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;

import java.io.PrintStream;
import java.util.Scanner;

public class SelectingNumberRequestsStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final Server server;

    public SelectingNumberRequestsStrategy(
            PrintStream printStream,
            Scanner scanner,
            Server server
    ) {
        this.printStream = printStream;
        this.scanner = scanner;
        this.server = server;
    }

    @Override
    public StrategyCLI apply() {
        printStream.print(Constants.SELECT_NUMBER_REQUESTS);
        int countRequests = scanner.nextInt();
        if (countRequests < 0) throw new IllegalArgumentException("Should be: number of requests >= 0");
        return new SelectOtherParameters(printStream, scanner, server, countRequests);
    }
}
