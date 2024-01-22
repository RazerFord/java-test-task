package ru.itmo.mit;

import java.util.List;

public class Constants {
    private Constants() {
    }

    public static final String ADDRESS = "0.0.0.0";
    public static final int PORT = 0;
    public static final List<Object> PARAMS = List.of("N", "M", "D");
    public static final String TEMPLATE_FILENAME_TXT = "%s_%s.txt";
    public static final String TEMPLATE_FILENAME_IMG = "%s_%s.png";
    public static final String FILENAME_DESC = "%s_description.txt";
    public static final String PROC_REQ = "processing_request_on_server";
    public static final String PROC_CLIENT = "processing_client_on_server";
    public static final String AVG_REQ_CLIENT = "average_request_time_on_client";
    public static final String PATH_TO_FILES = "./results";
    public static final int NUMBER_SIMULTANEOUS_CONNECTIONS = 10;
    public static final int NUMBER_WARMING_ITERATIONS = 2;
    public static final boolean LOG = false;

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
