package ru.itmo.mit;

import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.itmo.mit.Constants.*;

public class GraphicsSaver {
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
        var procReq = new LineChart(
                getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_PROC_REQ, nanos)),
                architectureName,
                ABSCISSA_PROC_REQ,
                axisName,
                ORDINATE
        );
        var procClient = new LineChart(
                getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_PROC_CLIENT, nanos)),
                architectureName,
                ABSCISSA_PROC_CLIENT,
                axisName,
                ORDINATE
        );
        var avgReqClient = new LineChart(
                getFile(TEMPLATE_FILENAME_IMG.formatted(PREFIX_AVG_REQ_CLIENT, nanos)),
                architectureName,
                ABSCISSA_AVG_REQ_CLIENT,
                axisName,
                ORDINATE
        );

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

    private static final class LineChart {
        private static final Font FONT = new Font("Dialog", Font.PLAIN, 20);
        private static final int WIDTH = 640;
        private static final int HEIGHT = 480;
        private final XYSeries xySeries;
        private final File file;
        private final String title;
        private final String x;
        private final String y;

        private LineChart(
                File file,
                String title,
                String key,
                String x,
                String y
        ) {
            xySeries = new XYSeries(key);
            this.file = file;
            this.title = title;
            this.x = x;
            this.y = y;
        }

        private void add(double x, double y) {
            xySeries.add(x, y);
        }

        private void save() throws IOException {
            var dataset = new XYSeriesCollection(xySeries);
            var render = new XYLineAndShapeRenderer();

            render.setSeriesLinesVisible(0, true);
            render.setSeriesPaint(0, Color.blue);
            render.setSeriesStroke(0, new BasicStroke(2));

            var chart = ChartFactory.createXYLineChart(title, x, y, dataset);
            var plot = chart.getXYPlot();

            plot.setRenderer(render);
            plot.setRangeGridlinesVisible(true);
            plot.setRangeGridlinePaint(Color.black);
            plot.setDomainGridlinesVisible(true);
            plot.setDomainGridlinePaint(Color.black);

            chart.getLegend().setItemFont(FONT);
            plot.getDomainAxis().setLabelFont(FONT);
            plot.getRangeAxis().setLabelFont(FONT);

            ChartUtils.saveChartAsJPEG(file, chart, WIDTH, HEIGHT);
        }
    }
}
