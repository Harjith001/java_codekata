package file.copier;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public interface FileCopier {

    /**
     * copies files from source to destination.
     * Implementation of this interface defines how the data is copied
     * (e.g. - FileStream, BufferedStream)
     * @param sourceFile    the path to the source file
     * @param destinationFile   the path to the destination file
     * @return  time elapsed
     * @throws IOException
     */
    long copy(File sourceFile, File destinationFile) throws IOException;
}

