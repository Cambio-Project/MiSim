package testutils;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public class FileLoaderUtil {

    @Contract("null -> fail")
    public static @NotNull File loadFromTestResources(final String... filepath) {
        Path basePath = Paths.get("src", "test", "resources");
        return loadFromFromFileSystem(basePath.toAbsolutePath().toString(), filepath);
    }

    @Contract("null -> fail")
    public static @NotNull File loadFromExampleResources(final String... filepath) {
        Path basePath = Paths.get("Examples");
        return loadFromFromFileSystem(basePath.toAbsolutePath().toString(), filepath);

    }

    @Contract("_, null -> fail")
    private static @NotNull File loadFromFromFileSystem(final String first, final String... filepath) {
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
