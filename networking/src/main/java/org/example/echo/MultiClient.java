package org.example.echo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MultiClient {
    public static String sendMessage(String message, int port) throws IOException {
        try (Socket socket = new Socket("127.0.0.1", port);
             DataOutputStream dataOut = new DataOutputStream(socket.getOutputStream());
             DataInputStream dataIn = new DataInputStream(socket.getInputStream())) {

            dataOut.writeUTF(message);
            return dataIn.readUTF();
        }
    }
}
