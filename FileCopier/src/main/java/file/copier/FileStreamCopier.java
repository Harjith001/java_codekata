package file.copier;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStreamCopier implements FileCopier{

    private final int byteArraySize;
    FileStreamCopier(){
        this(1);
    }

    FileStreamCopier(int byteArraySize){
        this.byteArraySize = byteArraySize;
    }

    @Override
    public long copy(File source, File dest) throws IOException {
        long start = System.nanoTime();

        try (FileInputStream fis = new FileInputStream(source);
             FileOutputStream fos = new FileOutputStream(dest)) {
            int data;
            byte[] buffer = new byte[byteArraySize];
            while ((data = fis.read(buffer)) != -1) {
                fos.write(buffer, 0, data);
            }
        }

        long end = System.nanoTime();
        return end - start;
    }
}
