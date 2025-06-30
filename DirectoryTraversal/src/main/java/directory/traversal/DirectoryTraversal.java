package directory.traversal;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DirectoryTraversal extends DirectoryAbstract {

    @Override
    public Stream<Path> listFiles(Path directory, String extension) {
        return traverseAndCollect(directory,
                file -> file.getName().toLowerCase().endsWith(extension.toLowerCase()));
    }

    @Override
    public Stream<Path> findContent(Path directory, String content) {
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
