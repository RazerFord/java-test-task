package ru.itmo.mit;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jfree.data.xy.XYSeries;

import java.io.File;
import java.io.IOException;

public class LineChart {
    private final LineCharts lineCharts;
    private final XYSeries xySeries;

    private LineChart(
            File file,
            String title,
            String key,
            String x,
            String y
    ) {
        lineCharts = LineCharts.builder().setFile(file).setTitle(title).setX(x).setY(y).build();
        xySeries = new XYSeries(key);
        lineCharts.add(xySeries);
    }

    public void add(double x, double y) {
        xySeries.add(x, y);
    }

    public void save() throws IOException {
        lineCharts.save();
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