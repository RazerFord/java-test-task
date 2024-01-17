package ru.mit.itmo.arraygenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IncreasingArrayGenerators implements ArrayGenerators {
    private final Random random;
    private int from;
    private final int to;
    private final int step;

    public IncreasingArrayGenerators(int from, int to, int step) {
        random = new Random();
        this.from = from;
        this.to = to;
        this.step = step;
    }

    @Override
    public List<Integer> generate() {
        List<Integer> numbers = new ArrayList<>(from);
        for (int i = 0; i < from; i++) {
            numbers.add(random.nextInt());
        }
        from = Integer.min(from + step, to);
        return numbers;
    }
}
