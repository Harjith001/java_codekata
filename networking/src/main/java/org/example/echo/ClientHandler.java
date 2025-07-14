package org.example.echo;

import java.util.List;

public class ClientHandler implements Runnable{

    private final List<ClientConnection> clients;

    public ClientHandler(List<ClientConnection> clients){

        this.clients = clients;
    }
    @Override
    public void run(){

    }

}
