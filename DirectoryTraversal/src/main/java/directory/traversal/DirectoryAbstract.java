package directory.traversal;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public abstract class DirectoryAbstract implements DirectoryOperations {
    protected Stream<Path> traverseAndCollect(Path directory, Predicate<File> filter) {
        File baseDir = new File(directory.toUri());
        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return Stream.empty();
        }

        Stream.Builder<Path> builder = Stream.builder();
        List<File> queue = new LinkedList<>();
        queue.add(baseDir);

        while (!queue.isEmpty()) {
            File current = queue.removeFirst();
            File[] files = current.listFiles();
            if (files == null) continue;

            for (File file : files) {
                if (file.isDirectory()) {
                    queue.add(file);
                } else if (file.isFile() && filter.test(file)) {
                    builder.add(file.toPath());
                }
            }
        }
        return builder.build();
    }
}
