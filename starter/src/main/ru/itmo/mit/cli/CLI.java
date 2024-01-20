package ru.itmo.mit.cli;

import ru.itmo.mit.ExitException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CLI {
    private StrategyCLI strategy;
    private final PrintStream printStream;
    private final InputStream inputStream;

    public CLI() {
        this(System.out, System.in);
    }

    public CLI(PrintStream printStream, InputStream inputStream) {
        this.printStream = printStream;
        this.inputStream = inputStream;
        strategy = new ServerArchitectureSelectionStrategy(printStream, new Scanner(inputStream));
    }

    public void start() {
        while (true) {
            try {
                strategy = strategy.apply();
            } catch (IllegalArgumentException | InputMismatchException e) {
                printStream.println(e.getMessage());
                strategy = new ServerArchitectureSelectionStrategy(printStream, new Scanner(inputStream));
            } catch (ExitException ignored) {
                break;
            }
        }
    }
}
