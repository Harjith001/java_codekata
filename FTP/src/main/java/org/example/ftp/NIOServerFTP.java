package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class NIOServerFTP extends FTPImplementation{
    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(NIOServerFTP.class);
    private Selector selector;
    private ServerSocketChannel serverChannel;


    private void start() throws IOException {
        selector = Selector.open();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while(keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();
                if( key.isAcceptable()) {
                    SocketChannel client = serverChannel.accept();
                    client.configureBlocking(false);
                    client.register(selector, SelectionKey.OP_READ);
                } else if(key.isReadable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    handleClient(client.socket());
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        NIOServerFTP server = new NIOServerFTP();
        server.start();
    }
}
