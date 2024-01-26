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

public class LineCharts {
    private static final Font FONT = new Font("Dialog", Font.PLAIN, 20);
    private static final Font FONT_TICK = new Font("Dialog", Font.PLAIN, 15);
    private static final BasicStroke BASIC_STROKE = new BasicStroke(2);
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final Color[] colors = {Color.blue, Color.green, Color.red};
    private int i = 0;
    private final XYSeriesCollection dataset = new XYSeriesCollection();
    private final XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
    private final File file;
    private final String title;
    private final String x;
    private final String y;

    private LineCharts(
            File file,
            String title,
            String x,
            String y
    ) {
        this.file = file;
        this.title = title;
        this.x = x;
        this.y = y;
    }

    public void add(XYSeries xySeries) {
        dataset.addSeries(xySeries);
        render.setSeriesLinesVisible(i, true);
        render.setSeriesPaint(i, getColor());
        render.setSeriesStroke(i, BASIC_STROKE);
        i++;
    }

    public void save() throws IOException {
        var chart = ChartFactory.createXYLineChart(title, x, y, dataset);
        var plot = chart.getXYPlot();

        plot.setRenderer(render);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.black);

        chart.getLegend().setItemFont(FONT);

        var domainAxis = plot.getDomainAxis();
        domainAxis.setLabelFont(FONT);
        domainAxis.setTickLabelFont(FONT_TICK);

        var rangeAxis = plot.getRangeAxis();
        rangeAxis.setLabelFont(FONT);
        rangeAxis.setTickLabelFont(FONT_TICK);
        ((NumberAxis) rangeAxis).setAutoRangeIncludesZero(false);

        ChartUtils.saveChartAsJPEG(file, chart, WIDTH, HEIGHT);
    }

    private Color getColor() {
        return colors[i % colors.length];
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {
        private File file;
        private String title;
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

        public Builder setX(String x) {
            this.x = x;
            return this;
        }

        public Builder setY(String y) {
            this.y = y;
            return this;
        }

        @Contract(value = " -> new", pure = true)
        public @NotNull LineCharts build() {
            return new LineCharts(file, title, x, y);
        }
    }
}