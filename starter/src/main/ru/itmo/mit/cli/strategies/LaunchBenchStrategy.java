package ru.itmo.mit.cli.strategies;

import ru.itmo.mit.ExitException;
import ru.itmo.mit.benchmarks.Benchmark;

import java.io.PrintStream;

public class LaunchBenchStrategy implements StrategyCLI {
    private final PrintStream printStream;
    private final Benchmark benchmark;

    public LaunchBenchStrategy(
            PrintStream printStream,
            Benchmark benchmark
    ) {
        this.printStream = printStream;
        this.benchmark = benchmark;
    }

    @Override
    public StrategyCLI apply() {
        benchmark.bench();
        printStream.println("Finish!" + " ".repeat(22));
        throw ExitException.INSTANCE;
    }
}
