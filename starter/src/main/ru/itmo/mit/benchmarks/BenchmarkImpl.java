package ru.itmo.mit.benchmarks;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import ru.itmo.mit.Constants;
import ru.itmo.mit.Server;
import ru.itmo.mit.asyncserver.AsyncServer;
import ru.itmo.mit.blockingserver.BlockingServer;
import ru.itmo.mit.nonblockingserver.NonBlockingServer;

public class BenchmarkImpl {
    private BenchmarkImpl() {
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int serverNumber;
        private int countRequest;
        private int numberParam;
        private int from;
        private int to;
        private int step;
        private int other1;
        private int other2;

        private Builder() {
        }

        public Builder setServerNumber(int architectureNumber) {
            serverNumber = architectureNumber;
            return this;
        }

        public Builder setCountRequest(int countRequest) {
            this.countRequest = countRequest;
            return this;
        }

        public Builder setNumberParam(int numberParam) {
            this.numberParam = numberParam;
            return this;
        }

        private Server createServer(int architectureNumber) {
            return switch (architectureNumber) {
                case 1 -> new BlockingServer(Constants.PORT);
                case 2 -> new NonBlockingServer(Constants.PORT);
                case 3 -> new AsyncServer(Constants.PORT);
                default -> throw new IllegalArgumentException(SELECTION_SERVER_ERROR);
            };
        }

        private static final String SELECTION_SERVER_ERROR = "There is no architecture with this number";
    }
}
