package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.itmo.mit.Constants.*;

public class GraphSaver {
    private final List<Integer> values = new ArrayList<>();
    private final List<Integer> processingRequest = new ArrayList<>();
    private final List<Integer> processingClient = new ArrayList<>();
    private final List<Integer> averageRequestProcessingTime = new ArrayList<>();
    private String description = "";

    public void append(@NotNull StatisticsRecorder statisticsRecorder) {
        values.add(statisticsRecorder.value());
        processingRequest.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_REQUEST));
        processingClient.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_CLIENT));
        averageRequestProcessingTime.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME));
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void save() throws IOException {
        var currTime = Instant.now();
        var nanos = currTime.getNano();
        try (
                var desc = new FileWriter(FILENAME_DESC.formatted(nanos));
                var procReq = new FileWriter(TEMPLATE_FILENAME.formatted(PREFIX_PROC_REQ, nanos));
                var procClient = new FileWriter(TEMPLATE_FILENAME.formatted(PREFIX_PROC_CLIENT, nanos));
                var avgReqClient = new FileWriter(TEMPLATE_FILENAME.formatted(PREFIX_AVG_REQ_CLIENT, nanos))
        ) {
            desc.write(description);
            for (int i = 0; i < values.size(); i++) {
                var value = values.get(i);
                procReq.write(PATTERN.formatted(value, processingRequest.get(i)));
                procClient.write(PATTERN.formatted(value, processingClient.get(i)));
                avgReqClient.write(PATTERN.formatted(value, averageRequestProcessingTime.get(i)));
            }
        }
    }

    private static final String PATTERN = "%s %s%n";
}
