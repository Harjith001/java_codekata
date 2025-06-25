package directory.traversal;

import java.util.List;

public interface DirectoryOperations {

    List<String> listFiles(String directory, String extension);

    List<String> findContent(String directory, String content);
}
