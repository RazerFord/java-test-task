package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GraphSaver {
    private final List<Integer> values = new ArrayList<>();
    private final List<Integer> processingRequest = new ArrayList<>();
    private final List<Integer> processingClient = new ArrayList<>();
    private final List<Integer> averageRequestProcessingTime = new ArrayList<>();

    public void append(@NotNull StatisticsRecorder statisticsRecorder) {
        values.add(statisticsRecorder.value());
        processingRequest.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_REQUEST));
        processingClient.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_CLIENT));
        averageRequestProcessingTime.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME));
    }

    public void save() throws IOException {
        var currTime = Instant.now();
        try (
                var fileWriter1 = new FileWriter("procRequest" + currTime + ".txt");
                var fileWriter2 = new FileWriter("procClient" + currTime + ".txt");
                var fileWriter3 = new FileWriter("avgClient" + currTime + ".txt")
        ) {
            for (int i = 0; i < values.size(); i++) {
                var value = values.get(i);
                fileWriter1.write(PATTERN.formatted(value, processingRequest.get(i)));
                fileWriter2.write(PATTERN.formatted(value, processingClient.get(i)));
                fileWriter3.write(PATTERN.formatted(value, averageRequestProcessingTime.get(i)));
            }
        }
    }

    private static final String PATTERN = "%s %s%n";
}
