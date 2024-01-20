package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.util.Queue;

public class StatisticsRecorder {
    private final FileEntries fileEntries = FileEntries.create();
    private final RecorderStrategy recorderStrategy;

    public StatisticsRecorder(int strategyNumber) {
        recorderStrategy = createRecorderStrategy(strategyNumber);
    }

    public void addRecord(
            int arrayLength,
            int numberClients,
            int delay,
            int time,
            ProcessingRequest ignored
    ) {
        recorderStrategy.addRecord(fileEntries.processingClient(), arrayLength, numberClients, delay, time);
    }

    public void addRecord(
            int arrayLength,
            int numberClients,
            int delay,
            int time,
            ProcessingClient ignored
    ) {
        recorderStrategy.addRecord(fileEntries.processingClient(), arrayLength, numberClients, delay, time);
    }

    public static class ProcessingRequest {
        public static final ProcessingRequest INSTANCE = new ProcessingRequest();

        private ProcessingRequest() {
        }
    }

    public static class ProcessingClient {
        public static final ProcessingRequest INSTANCE = new ProcessingRequest();

        private ProcessingClient() {
        }
    }

    private static RecorderStrategy createRecorderStrategy(int strategyNumber) {
        return switch (strategyNumber) {
            case 1 -> new RecordArrayLengthStrategy();
            case 2 -> new RecordNumberClientsStrategy();
            case 3 -> new RecordDelayStrategy();
            default -> throw new IllegalArgumentException();
        };
    }

    private interface RecorderStrategy {
        void addRecord(
                Queue<String> queue,
                int arrayLength,
                int numberClients,
                int delay,
                int time
        );
    }

    private static class RecordArrayLengthStrategy implements RecorderStrategy {
        @Override
        public void addRecord(
                @NotNull Queue<String> queue,
                int arrayLength,
                int numberClients,
                int delay,
                int time
        ) {
            queue.add(String.format(RECORDING_FORMAT, arrayLength, time));
        }
    }

    private static class RecordNumberClientsStrategy implements RecorderStrategy {
        @Override
        public void addRecord(
                @NotNull Queue<String> queue,
                int arrayLength,
                int numberClients,
                int delay,
                int time
        ) {
            queue.add(String.format(RECORDING_FORMAT, numberClients, time));
        }
    }

    private static class RecordDelayStrategy implements RecorderStrategy {
        @Override
        public void addRecord(
                @NotNull Queue<String> queue,
                int arrayLength,
                int numberClients,
                int delay,
                int time
        ) {
            queue.add(String.format(RECORDING_FORMAT, delay, time));
        }
    }

    private static final String RECORDING_FORMAT = "%s %s";
}
