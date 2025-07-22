package org.example.ftp;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPHandler implements Runnable {
    private final Socket commandSocket;
    private static final int DATA_PORT = 5002;
    private static final Logger LOG = (Logger) LogManager.getLogger(FTPHandler.class);

    public FTPHandler(Socket commandSocket) {
        this.commandSocket = commandSocket;
    }

    private boolean handleAuth(BufferedReader in, PrintWriter out) throws IOException {
        final String USERNAME = "charlie";
        final String PASSWORD = "123";

        out.println("Authentication required. Use: LOGIN <username> <password>");

        String line;
        int attempts = 3;

        while ((line = in.readLine()) != null && attempts > 0) {
            String[] parts = line.trim().split(" ");
            if (parts.length == 3 && parts[0].equalsIgnoreCase("LOGIN")) {
                String user = parts[1];
                String pass = parts[2];

                if (USERNAME.equals(user) && PASSWORD.equals(pass)) {
                    out.println("Login successful.");
                    return true;
                } else {
                    attempts--;
                    out.println("ERROR: Invalid credentials. Attempts left: " + attempts);
                }
            } else {
                out.println("Usage: LOGIN <username> <password>");
            }
        }

        out.println("ERROR: Too many failed attempts. Connection closing.");
        return false;
    }

    @Override
    public void run() {
        try (
                BufferedReader commandIn = new BufferedReader(new InputStreamReader(commandSocket.getInputStream()));
                PrintWriter commandOut = new PrintWriter(commandSocket.getOutputStream(), true)
        ) {
            if (!handleAuth(commandIn, commandOut)) {
                commandSocket.close();
                return;
            }

            String command;
            while ((command = commandIn.readLine()) != null) {
                String[] parts = command.split(" ");
                String cmd = parts[0].toUpperCase();

                switch (cmd) {
                    case "LIST":
                        handleList(commandOut);
                        break;
                    case "GET":
                        if (parts.length < 2) {
                            commandOut.println("ERROR: Missing filename");
                        } else {
                            handleGet(parts[1], commandOut);
                        }
                        break;
                    case "PUT":
                        if (parts.length < 2) {
                            commandOut.println("ERROR: Missing filename");
                        } else {
                            handlePut(parts[1], commandOut);
                        }
                        break;
                    default:
                        commandOut.println("ERROR: Unknown command");
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    private void handleList(PrintWriter out) {
        File folder = new File("server_directory");
        out.println("--------");
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    out.println(file.getName());
                }
            }
        }
        out.println("--------");
    }

    private void handleGet(String fileName, PrintWriter out) {
        File file = new File("server_directory", fileName);
        if (!file.exists()) {
            out.println("ERROR: File not found");
            return;
        }

        try (ServerSocket dataServerSocket = new ServerSocket(DATA_PORT)) {
            out.println("OK");
            out.println("Ready to send file. Connect to data port " + DATA_PORT);

            try (Socket dataSocket = dataServerSocket.accept();
                 DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());
                 FileInputStream fis = new FileInputStream(file)) {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = fis.read(buffer)) != -1) {
                    dataOut.write(buffer, 0, len);
                }

            } catch (IOException e) {
                out.println("ERROR: Failed during file transfer");
            }
        } catch (IOException e) {
            out.println("ERROR: Could not open data port");
        }
    }

    private void handlePut(String fileName, PrintWriter out) {
        try (ServerSocket dataServerSocket = new ServerSocket(DATA_PORT)) {
            out.println("OK");
            out.println("Ready to receive file. Connect to data port " + DATA_PORT);

            try (Socket dataSocket = dataServerSocket.accept();
                 DataInputStream dataIn = new DataInputStream(dataSocket.getInputStream());
                 FileOutputStream fos = new FileOutputStream(new File("server_directory", fileName))) {

                byte[] buffer = new byte[4096];
                int len;
                while ((len = dataIn.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

            } catch (IOException e) {
                out.println("ERROR: Failed during file reception");
            }
        } catch (IOException e) {
            out.println("ERROR: Could not open data port");
        }
    }
}
