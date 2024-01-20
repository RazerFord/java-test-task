package ru.itmo.mit.benchmarks;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;
import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.benchmarks.strategies.BenchArrayLengthStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchDelayStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchNumberClientsStrategy;
import ru.itmo.mit.benchmarks.strategies.BenchmarkStrategy;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;
import ru.mit.itmo.Client;
import ru.mit.itmo.arraygenerators.DefaultArrayGenerators;
import ru.mit.itmo.guard.DefaultGuard;
import ru.mit.itmo.waiting.DefaultWaiting;

import java.time.Duration;

public class BenchmarkImpl implements Benchmark {
    private final BenchmarkStrategy benchmarkStrategy;

    private BenchmarkImpl(BenchmarkStrategy benchmarkStrategy) {
        this.benchmarkStrategy = benchmarkStrategy;
    }

    @Override
    public void bench() {
        benchmarkStrategy.launch();
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
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
            var server = createServer(serverNumber);

            var builderClient = Client.builder()
                    .setTargetAddress(Constants.ADDRESS)
                    .setTargetPort(Constants.PORT)
                    .setCountRequest(countRequests);

            return switch (numberParam) {
                case 1 -> {
                    var guard = new DefaultGuard(other1);

                    builderClient
                            .setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(other2)))
                            .setGuardSupplier(() -> guard);
                    var benchmarkStrategy = new BenchArrayLengthStrategy(server, from, to, step, other1, builderClient);

                    yield new BenchmarkImpl(benchmarkStrategy);
                }
                case 2 -> {
                    builderClient
                            .setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(other1))
                            .setWaitingSupplier(() -> new DefaultWaiting(Duration.ofMillis(other2)));

                    var benchmarkStrategy = new BenchNumberClientsStrategy(server, from, to, step, builderClient);
                    yield new BenchmarkImpl(benchmarkStrategy);
                }

                case 3 -> {
                    var guard = new DefaultGuard(other2);

                    builderClient
                            .setArrayGeneratorsSupplier(() -> new DefaultArrayGenerators(other1))
                            .setGuardSupplier(() -> guard);

                    var benchmarkStrategy = new BenchDelayStrategy(server, from, to, step, other2, builderClient);

                    yield new BenchmarkImpl(benchmarkStrategy);
                }

                default -> throw new IllegalArgumentException(SELECTION_ERROR);
            };
        }

        private static Server createServer(int architectureNumber) {
            return switch (architectureNumber) {
                case 1 -> new BlockingServer(Constants.PORT);
                case 2 -> new NonBlockingServer(Constants.PORT);
                case 3 -> new AsyncServer(Constants.PORT);
                default -> throw SELECTION_ERROR;
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
