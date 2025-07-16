package org.example.ftp;

import java.io.*;

public abstract class FTPImplementation implements  FTPServer{
    private static final String DIR = "server_directory";
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
}
