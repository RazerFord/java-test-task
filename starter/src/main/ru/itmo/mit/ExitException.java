package ru.itmo.mit;

public class ExitException extends RuntimeException {
    public static final ExitException INSTANCE = new ExitException();
}
