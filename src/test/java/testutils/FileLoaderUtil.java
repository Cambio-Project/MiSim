package testutils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Lion Wagner
 */
public class FileLoaderUtil {

    public static File loadFromTestResources(final String... filepath) {
        Path basePath = Paths.get("src", "test", "resources");
        return loadFromFromFileSystem(basePath.toAbsolutePath().toString(), filepath);
    }

    public static File loadFromExampleResources(final String... filepath) {
        Path basePath = Paths.get("Examples");
        return loadFromFromFileSystem(basePath.toAbsolutePath().toString(), filepath);

    }

    private static File loadFromFromFileSystem(final String first, final String... filepath) {
        if (filepath == null || filepath.length == 0) {
            throw new InvalidPathException("", "Concrete path to resource is missing.");
        }

        File file = FileSystems.getDefault().getPath(first, filepath).toFile();
        if (!file.exists()) {
            throw new InvalidPathException(file.getAbsolutePath(), "Cannot find resource.");
        }
        return file;
    }
}
