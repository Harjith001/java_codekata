package org.example.echo;

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
    private static ThreadPoolEchoServer server;
    private static Thread serverThread;

    @BeforeAll
    static void startServer() throws InterruptedException {
        server = new ThreadPoolEchoServer();
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
    public void testMultipleClients() throws InterruptedException, ExecutionException {
        int clientCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 0; i < clientCount; i++) {
            int finalI = i;
            futures.add(executor.submit(() -> MultiClient.sendMessage("Hello" + finalI, PORT)));
        }

        for (int i = 0; i < clientCount; i++) {
            String expected = "Server's Hello" + i;
            String actual = futures.get(i).get();
            assertEquals(expected, actual, "Response mismatch for client " + i);
        }

        executor.shutdown();
    }
}
