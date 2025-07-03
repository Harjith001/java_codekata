package org.example.playground;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MultiClient {
    public static void main(String[] args) {
        String message = "Hello";

        for (int i = 0; i < 76000; i++) {
            int finalI = i;
            new Thread(() -> {
                try (Socket socket = new Socket("127.0.0.1", 5001);
                     DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
                     DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

                    dataOut.writeUTF(message + finalI);
                    String serverMessage = dataIn.readUTF();
                    System.out.println("Server message : " + serverMessage);
                    Thread.sleep(5000);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
