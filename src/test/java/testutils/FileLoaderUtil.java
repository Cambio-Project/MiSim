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

    public static File loadFromTestUtils(final String... filepath) {
        if (filepath == null || filepath.length == 0) {
            throw new InvalidPathException("", "Concrete path to resource is missing.");
        }

        Path basePath = Paths.get("src", "test", "resources");
        return FileSystems.getDefault().getPath(basePath.toAbsolutePath().toString(), filepath).toFile();
    }

}
