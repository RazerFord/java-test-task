package ru.itmo.mit.cli.strategies;

import ru.itmo.mit.Constants;
import ru.itmo.mit.GraphSaver;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.benchmarks.BenchmarkImpl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class SelectOtherParametersStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Scanner scanner;
    private final BenchmarkImpl.Builder benchmarkBuilder;

    public SelectOtherParametersStrategy(
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

        printStream.print(Constants.SELECT_STEP_FROM_TO);
        int from = scanner.nextInt();
        int to = scanner.nextInt();
        int step = scanner.nextInt();
        if (from < 0 || to < 0 || from > to || step <= 0) {
            throw new IllegalArgumentException("Should be: from >= 0, to >= 0, step > 0, from <= step");
        }
        benchmarkBuilder.setFrom(from).setTo(to).setStep(step);

        var params = new ArrayList<>(Constants.PARAMS);
        params.remove(numberParam - 1);

        printStream.printf(Constants.SELECT_OTHER_VALUES, params.toArray());
        int other1 = scanner.nextInt();
        int other2 = scanner.nextInt();
        benchmarkBuilder
                .setOther1(other1).setOther2(other2)
                .setStatisticsRecorder(new StatisticsRecorder())
                .setGraphSaver(new GraphSaver());

        return new LaunchBenchStrategy(printStream, benchmarkBuilder.build());
    }
}
