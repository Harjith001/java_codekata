package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SingleClientFTP implements FTPServer{
    private static final int PORT = 5001;
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(SingleClientFTP.class);


    @Override
    public void handleList(PrintWriter out) {
        File folder = new File(DIR);
        File[] files = folder.listFiles();
        if(files != null){
            for(File file : files) {
                if (file.isFile()) {
                    out.println(file.getName());
                }
            }
        }
    }

    @Override
    public void handleGet(String fileName, PrintWriter out, DataOutputStream dataOut){
        File file = new File(DIR, fileName);

        if(!file.exists()) {
            out.println("ERROR : File not found");
            return;
        }

        try{
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int len;
            while((len = fis.read(buffer)) != -1) {
                dataOut.write(buffer, 0, len);
            }
            fis.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handlePut(String fileName, BufferedReader in, DataInputStream dataIn) {
        File file = new File(DIR, fileName);
        try(FileOutputStream fos = new FileOutputStream(file)) {
            LOG.info("File is being sent to client : " + fileName);
            byte[] buffer = new byte[4096];
            int len;

            while((len = dataIn.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClient(Socket client) {
        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());
                DataInputStream dataIn = new DataInputStream(client.getInputStream());
                ) {
            String command;
            while ((command = in.readLine()) != null) {
                LOG.info("Command received from client : " + command);
                String[] split = command.split(" ");

                switch (split[0].toUpperCase()) {
                    case "LIST":
                        handleList(out);
                        break;
                    case "GET":
                        handleGet(split[1], out, dataOut);
                        break;
                    case "PUT":
                        handlePut(split[1], in, dataIn);
                        break;
                    default:
                        out.println("Error : unknown commands");
                        break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void start() throws IOException {
        try( ServerSocket serverSocket = new ServerSocket(PORT)){
            LOG.info("Server is listening at PORT 80");

            while (true) {
                try{
                    Socket clientSocket = serverSocket.accept();
                    LOG.info("Client is connected to the server");

                    handleClient(clientSocket);
                } catch (IOException e) {
                    LOG.error("Client connection error : {}", e.getMessage());
                }
            }
        } catch (IOException e) {
            LOG.error("Server start error : {}", e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        File dir = new File(DIR);
        if(!dir.exists()) {
            dir.mkdir();
        }

        SingleClientFTP server = new SingleClientFTP();
        server.start();
    }
}
