package directory.traversal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DirectoryTraversal extends DirectoryAbstract implements DirectoryOperations {

    @Override
    public List<String> listFiles(String directory, String extension) {
        return traverseAndCollect(directory,
                file -> file.getName().toLowerCase().endsWith(extension.toLowerCase()));
    }

    @Override
    public List<String> findContent(String directory, String content) {
        return traverseAndCollect(directory, file -> containsKeyword(file, content));
    }

    private boolean containsKeyword(File file, String keyword) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(keyword)) {
                    return true;
                }
            }
        } catch (IOException e) {
            // ignore unreadable files
        }
        return false;
    }
}
