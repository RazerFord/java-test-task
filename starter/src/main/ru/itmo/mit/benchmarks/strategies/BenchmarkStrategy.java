package ru.itmo.mit.benchmarks.strategies;

import ru.itmo.mit.Server;

import java.io.IOException;
import java.io.PrintStream;

public interface BenchmarkStrategy {
    void launch(Server server, PrintStream printStream) throws InterruptedException, IOException;
}
