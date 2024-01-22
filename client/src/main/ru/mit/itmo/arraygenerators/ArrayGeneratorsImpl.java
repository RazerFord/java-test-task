package ru.mit.itmo.arraygenerators;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ArrayGeneratorsImpl implements ArrayGenerators {
    private final Random random;
    private final int count;

    public ArrayGeneratorsImpl(int count) {
        random = new Random();
        this.count = count;
    }

    @Override
    public List<Integer> generate() {
        List<Integer> numbers = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            numbers.add(random.nextInt());
        }
        return numbers;
    }
}
