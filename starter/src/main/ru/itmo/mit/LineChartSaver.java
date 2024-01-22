package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.itmo.mit.Constants.*;

public class LineChartSaver {
    private static final String PATTERN = "%s %s%n";
    private static final String ORDINATE = "time";
    private static final String ABSCISSA_PROC_REQ = "request processing time";
    private static final String ABSCISSA_PROC_CLIENT = "client processing time";
    private static final String ABSCISSA_AVG_REQ_CLIENT = "average request time on client";
    private final List<Integer> values = new ArrayList<>();
    private final List<Integer> processingRequest = new ArrayList<>();
    private final List<Integer> processingClient = new ArrayList<>();
    private final List<Integer> averageRequestProcessingTime = new ArrayList<>();
    private final Path pathToFiles = Path.of(PATH_TO_FILES).toAbsolutePath();
    private String description = "";
    private String axisName = "";
    private String architectureName = "";

    public void append(@NotNull StatisticsRecorder statisticsRecorder) {
        values.add(statisticsRecorder.value());
        processingRequest.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_REQUEST));
        processingClient.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_CLIENT));
        averageRequestProcessingTime.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME));
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAxisName(String axisName) {
        this.axisName = axisName;
    }

    public void setArchitectureName(String architectureName) {
        this.architectureName = architectureName;
    }

    public void save() throws IOException {
        Files.createDirectories(pathToFiles);
        var nanos = Instant.now().getNano();
        saveFiles(nanos);
        saveGraphics(nanos);
    }

    private void saveFiles(int nanos) throws IOException {
        try (
                var desc = new FileWriter(getFile(FILENAME_DESC.formatted(nanos)));
                var procReq = new FileWriter(getFile(TEMPLATE_FILENAME_TXT.formatted(PREFIX_PROC_REQ, nanos)));
                var procClient = new FileWriter(getFile(TEMPLATE_FILENAME_TXT.formatted(PREFIX_PROC_CLIENT, nanos)));
                var avgReqClient = new FileWriter(getFile(TEMPLATE_FILENAME_TXT.formatted(PREFIX_AVG_REQ_CLIENT, nanos)))
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

    private void saveGraphics(int nanos) throws IOException {
        var builder = LineChart.builder().setTitle(architectureName).setX(axisName).setY(ORDINATE);

        var procReq = builder.setFile(getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_PROC_REQ, nanos)))
                .setKey(ABSCISSA_PROC_REQ).build();

        var procClient = builder.setFile(getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_PROC_CLIENT, nanos)))
                .setKey(ABSCISSA_PROC_CLIENT).build();

        var avgReqClient = builder.setFile(getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_AVG_REQ_CLIENT, nanos)))
                .setKey(ABSCISSA_AVG_REQ_CLIENT).build();

        for (int i = 0; i < values.size(); i++) {
            var value = values.get(i);
            procReq.add(value, processingRequest.get(i));
            procClient.add(value, processingClient.get(i));
            avgReqClient.add(value, averageRequestProcessingTime.get(i));
        }

        procReq.save();
        procClient.save();
        avgReqClient.save();
    }

    private @NotNull File getFile(String filename) {
        return pathToFiles.resolve(filename).toFile();
    }
}
