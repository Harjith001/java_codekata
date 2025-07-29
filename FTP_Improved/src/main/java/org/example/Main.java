package org.example;

import org.example.connection.FTPServer;

public class Main {
    public static void main(String[] args) {
        new FTPServer(2121).start();
    }
}