package ru.itmo.mit.cli.strategies;

import ru.itmo.mit.Constants;
import ru.itmo.mit.benchmarks.BenchmarkImpl;

import java.io.PrintStream;
import java.util.Scanner;

public class SelectingParametersFromToStepStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final BenchmarkImpl.Builder benchmarkBuilder;

    public SelectingParametersFromToStepStrategy(
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
        printStream.print(Constants.SELECT_STEP_FROM_TO);
        int from = scanner.nextInt();
        int to = scanner.nextInt();
        int step = scanner.nextInt();
        if (from < 0 || to < 0 || from > to || step <= 0) {
            throw new IllegalArgumentException("Should be: from >= 0, to >= 0, step > 0, from <= step");
        }
        benchmarkBuilder.setFrom(from).setTo(to).setStep(step);
        return new SelectingOtherParametersStrategy(printStream, scanner, benchmarkBuilder);
    }
}
