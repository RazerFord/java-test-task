package ru.itmo.mit.asyncserver;

import java.nio.channels.CompletionHandler;

public class WriteCallback implements CompletionHandler<Integer, AsyncHandler> {
    public static final WriteCallback INSTANCE = new WriteCallback();

    private WriteCallback() {
    }

    @Override
    public void completed(Integer result, AsyncHandler attachment) {

    }

    @Override
    public void failed(Throwable exc, AsyncHandler attachment) {

    }
}
