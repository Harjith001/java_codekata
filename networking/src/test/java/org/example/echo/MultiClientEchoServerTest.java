package org.example.echo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class MultiClientEchoServerTest {
    private static EchoServer server;
    private static final int TEST_PORT = 5001;

    @BeforeAll
    public static void setUp() throws Exception {
        server = new MultiClientEchoServer();
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                server.start(TEST_PORT);
            } catch (IOException e) {
                fail("Failed to start server: " + e.getMessage());
            }
        });
    }

    @AfterAll
    public static void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void testServerEchoesMessage() throws IOException {
        try (Socket socket = new Socket("127.0.0.1", TEST_PORT);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            String testMessage = "HelloServer";
            out.writeUTF(testMessage);
            String response = in.readUTF();

            assertEquals("Server's " + testMessage, response);
        }
    }
}
