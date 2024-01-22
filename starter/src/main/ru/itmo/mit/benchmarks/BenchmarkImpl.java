package ru.itmo.mit.benchmarks;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Constants;
import ru.itmo.mit.LineChartSaver;
import ru.itmo.mit.Server;
import ru.itmo.mit.StatisticsRecorder;
import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.benchmarks.strategies.BenchArrayLengthStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchDelayStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchNumberClientsStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchmarkStrategy;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.ArrayGeneratorsImpl;
import ru.mit.itmo.waiting.WaitingImpl;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.Objects;

import static ru.itmo.mit.Constants.NUMBER_WARMING_ITERATIONS;

public class BenchmarkImpl implements Benchmark {
    private final Server server;
    private final BenchmarkStrategy benchmarkStrategy;
    private final PrintStream printStream;

    private BenchmarkImpl(Server server, BenchmarkStrategy benchmarkStrategy, PrintStream printStream) {
        this.server = server;
        this.benchmarkStrategy = benchmarkStrategy;
        this.printStream = printStream;
    }

    @Override
    public void bench() {
        try {
            var threadServer = new Thread(server);
            threadServer.start();
            benchmarkStrategy.launch(server.getPort(), printStream);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            try {
                if (server != null) {
                    server.close();
                }
            } catch (IOException ignored) {
                // is the block empty on purpose
            }
        }
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private PrintStream printStream;
        private int serverNumber;
        private int countRequests;
        private int numberParam;
        private int from;
        private int to;
        private int step;
        private int other1;
        private int other2;

        private Builder() {
        }

        public int getNumberParam() {
            return numberParam;
        }

        public Builder setPrintStream(PrintStream printStream) {
            Objects.requireNonNull(printStream);
            this.printStream = printStream;
            return this;
        }

        public Builder setServerNumber(int architectureNumber) {
            throwIf(architectureNumber < 1 || architectureNumber > 3, SELECTION_ERROR);
            serverNumber = architectureNumber;
            return this;
        }

        public Builder setCountRequests(int countRequests) {
            throwIf(countRequests < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.countRequests = countRequests;
            return this;
        }

        public Builder setNumberParam(int numberParam) {
            throwIf(numberParam < 1 || numberParam > 3, Constants.PARAMETER_NOT_NEGATIVE);
            this.numberParam = numberParam;
            return this;
        }

        public Builder setFrom(int from) {
            throwIf(from < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.from = from;
            return this;
        }

        public Builder setTo(int to) {
            throwIf(to < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.to = to;
            return this;
        }

        public Builder setStep(int step) {
            throwIf(step < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.step = step;
            return this;
        }

        public Builder setOther1(int other1) {
            throwIf(other1 < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.other1 = other1;
            return this;
        }

        public Builder setOther2(int other2) {
            throwIf(other2 < 0, Constants.PARAMETER_NOT_NEGATIVE);
            this.other2 = other2;
            return this;
        }

        public BenchmarkImpl build() {
            throwIf(from > to, new IllegalArgumentException("Should be: from >= 0, to >= 0, step > 0, from <= step"));
            var statisticsRecorder = new StatisticsRecorder();
            var lineChartSaver = new LineChartSaver(createDescription(), createAxisName(), createArchitectureName());
            var server = createServer(statisticsRecorder);
            var clientBuilder = createClientBuilder(statisticsRecorder);
            return new BenchmarkImpl(server, createBenchStrategy(clientBuilder, statisticsRecorder, lineChartSaver), printStream);
        }

        private Server createServer(StatisticsRecorder statisticsRecorder) {
            int backlog = createBacklog();
            return switch (serverNumber) {
                case 1 -> new BlockingServer(Constants.PORT, backlog, statisticsRecorder);
                case 2 -> new NonBlockingServer(Constants.PORT, backlog, statisticsRecorder);
                case 3 -> new AsyncServer(Constants.PORT, backlog, statisticsRecorder);
                default -> throw SELECTION_ERROR;
            };
        }

        private Client.Builder createClientBuilder(StatisticsRecorder statisticsRecorder) {
            var builder = Client.builder()
                    .setTargetAddress(Constants.ADDRESS)
                    .setCountRequest(countRequests)
                    .setStatisticsRecorderSupplier(() -> statisticsRecorder);

            return switch (numberParam) {
                case 1 -> builder.setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(other2)));
                case 2 -> builder.setArrayGeneratorsSupplier(() -> new ArrayGeneratorsImpl(other1))
                        .setWaitingSupplier(() -> new WaitingImpl(Duration.ofMillis(other2)));
                case 3 -> builder.setArrayGeneratorsSupplier(() -> new ArrayGeneratorsImpl(other1));
                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private BenchmarkStrategy createBenchStrategy(
                Client.Builder clientBuilder,
                StatisticsRecorder statisticsRecorder,
                LineChartSaver lineChartSaver
        ) {
            var fromToStep = new FromToStep(from, to, step);
            return switch (numberParam) {
                case 1 -> new BenchArrayLengthStrategy(fromToStep, other1, clientBuilder, statisticsRecorder,
                        lineChartSaver, NUMBER_WARMING_ITERATIONS);
                case 2 -> new BenchNumberClientsStrategy(fromToStep, clientBuilder, statisticsRecorder,
                        lineChartSaver, NUMBER_WARMING_ITERATIONS);
                case 3 -> new BenchDelayStrategy(fromToStep, other2, clientBuilder, statisticsRecorder,
                        lineChartSaver, NUMBER_WARMING_ITERATIONS);
                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private String createDescription() {
            String fromToStep = "%s .. %s step %s".formatted(from, to, step);
            return switch (numberParam) {
                case 1 -> Constants.DESCRIPTION.formatted(serverNumber, countRequests, fromToStep, other1, other2);
                case 2 -> Constants.DESCRIPTION.formatted(serverNumber, countRequests, other1, fromToStep, other2);
                case 3 -> Constants.DESCRIPTION.formatted(serverNumber, countRequests, other1, other2, fromToStep);
                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private String createArchitectureName() {
            return switch (serverNumber) {
                case 1 -> "Blocking architecture";
                case 2 -> "Non-blocking architecture";
                case 3 -> "Asynchronous architecture";
                default -> throw SELECTION_ERROR;
            };
        }

        private String createAxisName() {
            return switch (numberParam) {
                case 1 -> "array length";
                case 2 -> "number of clients";
                case 3 -> "time period from receiving to sending a message, ms";
                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private int createBacklog() {
            return switch (numberParam) {
                case 1 -> other1;
                case 2 -> to;
                case 3 -> other2;
                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private static <T extends RuntimeException> void throwIf(boolean cond, T exception) {
            if (cond) {
                throw exception;
            }
        }

        private static final IllegalArgumentException SELECTION_ERROR = new IllegalArgumentException("The parameter number is incorrect");
    }
}
