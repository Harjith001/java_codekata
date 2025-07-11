package org.example.echo;

import org.example.playground.SingleThreadMultiClientEchoServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiClientTest {

    private static final int PORT = 5001;
    private static EchoServer server;
    private static Thread serverThread;

    /*
    @BeforeAll
    static void startServer() throws InterruptedException {
        server = new SingleThreadMultiClientEchoServer();
        serverThread = new Thread(() -> {
            try {
                server.start(PORT);
            } catch (IOException e) {
                System.out.println("IOException");
            }
        });
        serverThread.start();

        Thread.sleep(500);
    }
        */
    @AfterAll
    static void stopServer() throws IOException, InterruptedException {
        if (server != null) {
            server.stop();
        }
        if (serverThread != null) {
            serverThread.join();
        }
    }

    @Test
    public void testSendMessage() throws IOException {
        String message = "HelloTest";
        String response = MultiClient.sendMessage(message, PORT);
        assertEquals("Server's " + message, response);
    }

    @Test
    public void testMultipleClients() throws InterruptedException, ExecutionException, TimeoutException {
        int clientCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < clientCount; i++) {
            int finalI = i;
            futures.add(executor.submit(() -> {
                try {
                    // Add small delay between client connections
                    Thread.sleep(10);
                    return MultiClient.sendMessage("Hello" + finalI + "\n", PORT);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return "Error: " + e.getMessage();
                }
            }));
        }

        for (int i = 0; i < clientCount; i++) {
            String expected = "Server's Hello" + i;
            String actual = futures.get(i).get(10, TimeUnit.SECONDS);
            System.out.println("Client " + i + " response: " + actual);
            assertEquals(expected, actual, "Response mismatch for client " + i);
        }

        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
    }
}
