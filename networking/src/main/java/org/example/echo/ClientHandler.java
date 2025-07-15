package org.example.echo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable{

    private static final Logger LOG = LogManager.getLogger(ClientHandler.class);

    private final BlockingQueue<ClientConnection> clients;

    public ClientHandler(LinkedBlockingQueue<ClientConnection> clients){
        this.clients = clients;
    }
    @Override
    public void run(){
        while (true) {
            ClientConnection newClient;

            Iterator<ClientConnection> iterator = clients.iterator();
            while (iterator.hasNext()) {
                ClientConnection client = iterator.next();

                try {
                    client.socket.setSoTimeout(5000);
                    int available = client.inputStream.available();

                    if (available > 0) {
                        byte[] buffer = new byte[Math.min(available, 4096)];
                        client.outputStream.write(buffer);
                    }
                } catch (IOException e) {
                    LOG.error("Client connection error: {}", e.getMessage());
                    client.close();
                    iterator.remove();
                }
            }
        }
    }
}
