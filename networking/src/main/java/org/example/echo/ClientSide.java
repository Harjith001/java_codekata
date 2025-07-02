package org.example.echo;

import java.net.*;
import java.io.*;

public class ClientSide {
    private Socket socket = null;
    private DataInputStream input = null;
    private DataOutputStream out = null;

    public ClientSide(String address, int port) {
        try {
            socket = new Socket(address, port);
            System.out.println("Connected");
            input = new DataInputStream(System.in);
            out = new DataOutputStream(socket.getOutputStream());
        } catch(UnknownHostException u) {
            System.out.println(u);
        } catch(IOException i) {
            System.out.println(i);
        }

        String line = "";
        while (!line.equals("Over")) {
            try {
                assert input != null;
                line = input.readLine();
                out.writeUTF(line);
            } catch(IOException i) {
                System.out.println(i);
            }
        }

        try {
            input.close();
            out.close();
            socket.close();
        } catch(IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String args[]) {
        ClientSide client = new ClientSide("127.0.0.1", 5000);
    }
}