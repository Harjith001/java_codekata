package directory.traversal;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public abstract class DirectoryAbstract implements DirectoryOperations {
    protected List<Path> traverseAndCollect(Path directory, Predicate<File> filter) {
        List<Path> result = new ArrayList<>();
        File baseDir = new File(directory.toUri());

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return result;
        }

        List<File> queue = new ArrayList<>();
        queue.add(baseDir);

        while (!queue.isEmpty()) {
            File current = queue.removeFirst();
            File[] files = current.listFiles();

            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.isFile() && filter.test(file)) {
                    result.add(file.toPath());
                }
            }
        }

        return result;
    }
}
