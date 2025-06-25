package directory.traversal;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DirectoryTraversal implements DirectoryOperations {

    @Override
    public List<String> listFiles(String directory, String extension) {
        List<String> result = new ArrayList<>();
        File baseDir = new File(directory);
        if (baseDir.exists() && baseDir.isDirectory()) {
            traverseAndCollectByExtension(baseDir, extension.toLowerCase(), result);
        }
        return result;
    }

    private void traverseAndCollectByExtension(File rootDir, String extension, List<String> result) {
        List<File> queue = new ArrayList<>();
        queue.add(rootDir);

        while (!queue.isEmpty()) {
            File currentDir = queue.removeFirst();
            File[] files = currentDir.listFiles();

            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.getName().toLowerCase().endsWith(extension)) {
                    result.add(file.getAbsolutePath());
                }
            }
        }
    }


    @Override
    public List<String> findContent(String directory, String content) {
        List<String> result = new ArrayList<>();
        File baseDir = new File(directory);
        if (baseDir.exists() && baseDir.isDirectory()) {
            traverseAndSearchContent(baseDir, content, result);
        }
        return result;
    }

    private void traverseAndSearchContent(File dir, String keyword, List<String> result) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                traverseAndSearchContent(file, keyword, result);
            } else if (file.isFile()) {
                if (containsKeyword(file, keyword)) {
                    result.add(file.getAbsolutePath());
                }
            }
        }
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
            //ignore unreadable files
        }
        return false;
    }
}
