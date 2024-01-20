package ru.itmo.mit.cli.strategies;

import ru.itmo.mit.Constants;
import ru.itmo.mit.benchmarks.BenchmarkImpl;

import java.io.PrintStream;
import java.util.Scanner;

public class SelectingNumberRequestsStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final BenchmarkImpl.Builder benchmarkBuilder;

    public SelectingNumberRequestsStrategy(
            PrintStream printStream,
            Scanner scanner,
            BenchmarkImpl.Builder benchmarkBuilder
    ) {
        this.printStream = printStream;
        this.scanner = scanner;
        this.benchmarkBuilder = benchmarkBuilder;
    }

    @Override
    public StrategyCLI apply() {
        printStream.print(Constants.SELECT_NUMBER_REQUESTS);
        int countRequests = scanner.nextInt();
        benchmarkBuilder.setCountRequests(countRequests);
        return new SelectOtherParametersStrategy(printStream, scanner, benchmarkBuilder);
    }
}
