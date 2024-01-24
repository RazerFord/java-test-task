package ru.itmo.mit;

import ru.itmo.mit.cli.CLI;

import java.util.logging.LogManager;

public class Main {
    public static void main(String[] args) {
        if (!Constants.LOG) LogManager.getLogManager().reset();
        CLI.create().start();
    }
}