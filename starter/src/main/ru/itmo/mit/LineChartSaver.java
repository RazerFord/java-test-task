package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static ru.itmo.mit.Constants.*;

public class LineChartSaver {
    private static final String PATTERN = "%s %s%n";
    private static final String ORDINATE = "time, ms";
    private static final String ABSCISSA_PROC_REQ = "request processing time on server";
    private static final String ABSCISSA_PROC_CLIENT = "client processing time on server";
    private static final String ABSCISSA_AVG_REQ_CLIENT = "average request time on client";
    private final List<Integer> values = new ArrayList<>();
    private final List<Integer> processingRequest = new ArrayList<>();
    private final List<Integer> processingClient = new ArrayList<>();
    private final List<Integer> averageRequestProcessingTime = new ArrayList<>();
    private final Path pathToFiles;
    private final Path pathToImages = Path.of(PATH_TO_IMAGES).toAbsolutePath();
    private final String description;
    private final String axisName;
    private final String architectureName;

    public LineChartSaver(String description, String axisName, @NotNull String architectureName) {
        this.pathToFiles = Path.of(PATH_TO_FILES).resolve(String.join("_", architectureName.toLowerCase().split("( )+"))).toAbsolutePath();
        this.description = description;
        this.axisName = axisName;
        this.architectureName = architectureName;
    }

    public void append(@NotNull StatisticsRecorder statisticsRecorder) {
        values.add(statisticsRecorder.value());
        processingRequest.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_REQUEST));
        processingClient.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_PROCESSING_CLIENT));
        averageRequestProcessingTime.add(statisticsRecorder.average(StatisticsRecorder.SELECTOR_AVG_REQ_PROCESSING_TIME));
    }

    public void save() throws IOException {
        var fileNamePrefix = String.join("_", architectureName.toLowerCase().split("( )+"));
        var changeableParameter = String.join("_", axisName.toLowerCase()
                .replaceAll("[^a-zA-Z0-9\\-\\s]", "").split("( )+"));
        Files.createDirectories(pathToFiles);
        Files.createDirectories(pathToImages);
        saveFiles(fileNamePrefix, changeableParameter);
        saveGraphics(fileNamePrefix, changeableParameter);
    }

    private void saveFiles(String fileNamePrefix, String changeableParameter) throws IOException {
        var template = TEMPLATE_FILENAME_TXT.formatted(fileNamePrefix, changeableParameter, "%s");
        try (
                var desc = new FileWriter(getFile(FILENAME_DESC.formatted(fileNamePrefix, changeableParameter)));
                var procReq = new FileWriter(getFile(template.formatted(PROC_REQ)));
                var procClient = new FileWriter(getFile(template.formatted(PROC_CLIENT)));
                var avgReqClient = new FileWriter(getFile(template.formatted(AVG_REQ_CLIENT)))
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

    private void saveGraphics(String fileNamePrefix, String changeableParameter) throws IOException {
        var template = TEMPLATE_FILENAME_IMG.formatted(fileNamePrefix, changeableParameter, "%s");
        var builder = LineChart.builder().setTitle(fileNamePrefix).setX(axisName).setY(ORDINATE);

        var procReq = builder.setFile(getImage(template.formatted(PROC_REQ)))
                .setKey(ABSCISSA_PROC_REQ).build();

        var procClient = builder.setFile(getImage(template.formatted(PROC_CLIENT)))
                .setKey(ABSCISSA_PROC_CLIENT).build();

        var avgReqClient = builder.setFile(getImage(template.formatted(AVG_REQ_CLIENT)))
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

    private @NotNull File getImage(String imageName) {
        return pathToImages.resolve(imageName).toFile();
    }
}
