package ru.itmo.mit.cli.strategies;

import ru.itmo.mit.Constants;
import ru.itmo.mit.benchmarks.BenchmarkImpl;

import java.io.PrintStream;
import java.util.Scanner;

public class SelectingChangeableParameterStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final BenchmarkImpl.Builder benchmarkBuilder;

    public SelectingChangeableParameterStrategy(
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
        printStream.print(Constants.SELECT_CHANGEABLE_PARAM);
        int numberParam = scanner.nextInt();
        benchmarkBuilder.setNumberParam(numberParam);
        return new SelectingParametersFromToStepStrategy(printStream, scanner, benchmarkBuilder);
    }
}
