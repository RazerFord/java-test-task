package ru.itmo.mit;

public class Constants {
    private Constants() {
    }

    public static final String ADDRESS = "0.0.0.0";
    public static final int PORT = 8081;
    public static final Object[] PARAMS = {"N", "M", "Δ"};

    public static final String SELECT_ARCHITECTURE = """
            Select architecture:
            1. Blocking
            2. Non-blocking
            3. Asynchronous
            """;

    public static final String SELECT_NUMBER_REQUESTS = """
            Select number of requests:
            """;

    public static final String SELECT_CHANGEABLE_PARAM = """
            Select parameter to change:
            1. N - Number of elements in the array
            2. M - Number of working clients
            3. Δ - Time interval between requests, ms
            """;

    public static final String SELECT_STEP_FROM_TO = """
            Select from, to and step
            """;

    public static final String SELECT_OTHER_VALUES = """
            Select other values: %s, %s
            N - Number of elements in the array
            M - Number of working clients
            Δ - Time interval between requests, ms
            """;

    public static final IllegalArgumentException PARAMETER_NOT_NEGATIVE = new IllegalArgumentException("Parameter must not be negative");
}
