package ru.itmo.mit.cli;

import ru.itmo.mit.Constants;
import ru.itmo.mit.benchmarks.BenchmarkImpl;

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
        printStream.print(Constants.SELECT_ARCHITECTURE);
        var benchmarkBuilder = BenchmarkImpl.builder();
        int serverNumber = scanner.nextInt();
        benchmarkBuilder.setServerNumber(serverNumber);
        return new SelectingNumberRequestsStrategy(printStream, scanner, benchmarkBuilder);
    }
}
