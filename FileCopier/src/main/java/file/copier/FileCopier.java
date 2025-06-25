package file.copier;

import java.io.IOException;

public interface FileCopier {

    long copy(String sourceFile, String destinationFile) throws IOException;
}

