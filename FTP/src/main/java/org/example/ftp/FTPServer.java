package org.example.ftp;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.PrintWriter;

/**
 * Contract defines the basic functionalities of a FTP server
 */
public interface FTPServer {

    void handleList(PrintWriter out);

    void handleGet(String fileName, PrintWriter out, DataOutputStream dataOut);

    void handlePut(String fileName, BufferedReader in, DataInputStream datIn);
}
