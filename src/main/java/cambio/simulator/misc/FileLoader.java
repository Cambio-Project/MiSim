package cambio.simulator.misc;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

/**
 * Utility class for checking file existence.
 *
 * @author Lion Wagner
 */
public final class FileLoader {
    /**
     * Tries to create a new {@link File} reference to the given path.
     *
     * @param filepath path to the file.
     * @return the given path as existing {@link File}.
     * @throws FileNotFoundException if the given path does not exist.
     * @throws FileNotFoundException if the given path is a directory.
     */
    public static File tryLoadExistingFile(String filepath) throws FileNotFoundException {
        if (filepath == null) {
            throw new FileNotFoundException(null);
        }
        File f = new File(filepath);
        if (!f.exists()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        if (!f.isFile()) {
            throw new FileNotFoundException(f.getAbsolutePath());
        }
        return f;
    }

    /**
     * Tries to create a new {@link File} reference to the given path.
     *
     * @param filepath path to the file.
     * @return the given path as existing {@link File}.
     * @throws FileNotFoundException if the given path does not exist or is a directory.
     */
    public static File tryLoadExistingFile(Path filepath) throws FileNotFoundException {
        return tryLoadExistingFile(filepath.toString());
    }
}
