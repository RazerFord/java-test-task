package ru.itmo.mit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class LineChart {
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

    public void add(double x, double y) {
        xySeries.add(x, y);
    }

    public void save() throws IOException {
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
        ((NumberAxis)plot.getRangeAxis()).setAutoRangeIncludesZero(false);

        ChartUtils.saveChartAsJPEG(file, chart, WIDTH, HEIGHT);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private File file;
        private String title;
        private String key;
        private String x;
        private String y;

        private Builder() {
        }

        public Builder setFile(File file) {
            this.file = file;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setX(String x) {
            this.x = x;
            return this;
        }

        public Builder setY(String y) {
            this.y = y;
            return this;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull LineChart build() {
            return new LineChart(file, title, key, x, y);
        }
    }
}