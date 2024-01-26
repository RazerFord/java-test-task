package ru.itmo.mit.benchmarks.strategies;

import ru.mit.itmo.Client;

class ClientLauncher {
    private final Client.Builder clientBuilder;
    private final Thread[] threadsClient;
    private final Client[] clients;

    public ClientLauncher(int numberClients, Client.Builder clientBuilder) {
        this.clientBuilder = clientBuilder;
        threadsClient = new Thread[numberClients];
        clients = new Client[numberClients];
    }

    public void launch() throws InterruptedException {
        int length = threadsClient.length;
        for (int i = 0; i < length; i++) {
            var client = clientBuilder.build();
            clients[i] = client;
            var thread = new Thread(client);
            threadsClient[i] = thread;
            thread.start();
        }
        for (Thread thread : threadsClient) {
            thread.join();
        }
    }

    public Client[] getClients() {
        return clients;
    }
}