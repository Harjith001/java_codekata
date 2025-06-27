package directory.traversal;

import java.nio.file.Path;
import java.util.List;


public interface DirectoryOperations {
    /**
     *
     * Interface to define directory operations
     * Listing all files under a directory with a particular extension
     * Listing all files with the keyword - keyword search
     * @param directory     Directory to search
     * @param extension     Extension to search e.g. - .txt, .html
     * @return      List of file names
     */
    List<Path> listFiles(Path directory, String extension);

    List<Path> findContent(Path directory, String content);
}
