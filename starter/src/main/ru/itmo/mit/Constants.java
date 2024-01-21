package ru.itmo.mit;

import java.util.List;

public class Constants {
    private Constants() {
    }

    public static final String ADDRESS = "0.0.0.0";
    public static final int PORT = 0;
    public static final List<Object> PARAMS = List.of("N", "M", "D");
    public static final String TEMPLATE_FILENAME = "%s_%s.txt";
    public static final String FILENAME_DESC = "desc_%s.txt";
    public static final String PREFIX_PROC_REQ = "proc_request";
    public static final String PREFIX_PROC_CLIENT = "proc_client";
    public static final String PREFIX_AVG_REQ_CLIENT = "avg_req_client";

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
            3. D - Time interval between requests, ms
            """;

    public static final String SELECT_STEP_FROM_TO = """
            Select from, to and step
            """;

    public static final String SELECT_OTHER_VALUES = """
            Select other values: %s, %s
            N - Number of elements in the array
            M - Number of working clients
            D - Time interval between requests, ms
            """;

    public static final String DESCRIPTION = """
            Architecture:
                1. Blocking
                2. Non-blocking
                3. Asynchronous
            
            Parameters:
                S - Selected architecture
                X - Total number of requests sent by each client
                N - Number of elements in the array
                M - Number of working clients
                D - Time interval between requests, ms

            S = %s
            X = %s
            N = %s
            M = %s
            D = %s
            """;

    public static final IllegalArgumentException PARAMETER_NOT_NEGATIVE = new IllegalArgumentException("Parameter must not be negative");
}
