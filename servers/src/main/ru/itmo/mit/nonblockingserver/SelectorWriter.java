package ru.itmo.mit.nonblockingserver;

import java.nio.channels.Selector;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class SelectorWriter implements Runnable {
    private final Selector selector;

    public SelectorWriter(Selector selector) {
        this.selector = selector;
    }

    @Override
    public void run() {

    }
}
