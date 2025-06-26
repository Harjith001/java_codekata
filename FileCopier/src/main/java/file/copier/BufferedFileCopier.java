package file.copier;

import java.io.*;

public class BufferedFileCopier implements FileCopier{

    private final int bufferSize;

    public BufferedFileCopier(int bufferSize){
        this.bufferSize = bufferSize;
    }

    @Override
    public long copy(String source, String dest) throws IOException {

        long start = System.nanoTime();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(source), bufferSize);
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dest), bufferSize)) {

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        }
        long end = System.nanoTime();
        return end - start;
    }
}
