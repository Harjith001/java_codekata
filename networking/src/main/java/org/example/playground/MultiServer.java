package org.example.playground;

import javax.sound.midi.SysexMessage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5001);
        System.out.println("Server listening at port 5001");

        while(true){
            Socket connectionSocket = serverSocket.accept();
            new Thread(() ->{
                try {
                    DataInputStream dataIn = new DataInputStream(connectionSocket.getInputStream());
                    DataOutputStream dataOut = new DataOutputStream(connectionSocket.getOutputStream());

                    String messageReceived = dataIn.readUTF();
                    System.out.println("Clients message : "+ messageReceived);
                    dataOut.writeUTF("Server's " + messageReceived);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}
