package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.net.Socket;

public abstract class FTPImplementation implements  FTPServer{
    private static final String DIR = "server_directory";
    private static final Logger LOG = (Logger) LogManager.getLogger(FTPImplementation.class);

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
            LOG.info("File is being sent to client : {}", fileName);
            byte[] buffer = new byte[4096];
            int len;

            while((len = dataIn.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean handleAuth(BufferedReader in, PrintWriter out) throws IOException {
        final String username = "charlie";
        final String password = "123";

        out.println("Authentication : LOGIN <username> <password>");

        String line;

        while ((line = in.readLine()) != null) {
            String[] split = line.trim().split(" ");
            if (split.length == 3 && split[0].equalsIgnoreCase("LOGIN")) {
                String user = split[1];
                String pass = split[2];

                if (user.equals(username) && pass.equals(password)) {
                    out.println("Login successful.");
                    LOG.info("Client authenticated successfully.");
                    return true;
                } else {
                    out.println("ERROR: Invalid credentials.");
                    LOG.warn("Authentication failed for user: {}", user);
                }
            } else {
                out.println("Login using this format : LOGIN <username> <password>");
            }
        }

        return false;
    }

    protected void handleClient(Socket client) {
        try(
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
                DataOutputStream dataOut = new DataOutputStream(client.getOutputStream());
                DataInputStream dataIn = new DataInputStream(client.getInputStream());
        ) {

            if (!handleAuth(in, out)) {
                LOG.warn("Authentication failed or client disconnected.");
                return;
            }

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
}
