package cambio.simulator.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.CopyOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Modified version of the JarUtil class of
 * <a href="https://github.com/TriggerReactor/TriggerReactor/">TriggerReactor</a>.
 *
 * <p>
 * This modified version does support the loading of resourced from the /target/sources directory during tests or
 * debugging.
 *
 * <p>
 * This class is partially licenced under the GNU General Public License.
 *
 * @author <a href="https://github.com/wysohn">wysohn</a>
 */

public final class JarUtil {
    public static final char JAR_SEPARATOR = '/';


    /**
     * Copies a resource directory form the jar to  the given destination.
     *
     * @throws IOException if the target directory cannot be read properly.
     */
    public static void copyFolderFromJar(String folderName, File destFolder) throws IOException {
        copyFolderFromJar(folderName, destFolder, null, null);
    }


    /**
     * Copies a resource directory form the jar to  the given destination.
     *
     * @throws IOException if the target directory cannot be read properly.
     */
    public static void copyFolderFromJar(String folderName, File destFolder, CopyOption option) throws IOException {
        copyFolderFromJar(folderName, destFolder, option, null);
    }


    /**
     * Copies a resource directory form the jar to  the given destination.
     *
     * @throws IOException if the target directory cannot be read properly.
     */
    public static void copyFolderFromJar(String folderName, File destFolder, CopyOption option, PathTrimmer trimmer)
        throws IOException {
        if (!destFolder.exists()) {
            boolean ignored = destFolder.mkdirs();
        }

        File fullPath = null;
        String path = JarUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (trimmer != null) {
            path = trimmer.trim(path);
        }
        try {
            if (!path.startsWith("file")) {
                path = "file://" + path;
            }

            fullPath = new File(new URI(path));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        try {
            copyFromJar(folderName, destFolder, option, fullPath);
        } catch (IOException e) {
            // ZipInputStream throws a message ending in '(Access is denied)' if it tries to read
            // a file stream from a non-zip/jar file
            if (e.getMessage().endsWith("(Access is denied)")) {
                copyFromDirectory(folderName, destFolder, option, fullPath);
            } else {
                throw e;
            }
        }
    }

    private static void copyFromJar(String folderName, File destFolder, CopyOption option, File fullPath)
        throws IOException {

        byte[] buffer = new byte[1024];

        ZipInputStream zis = new ZipInputStream(new FileInputStream(fullPath));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.getName().startsWith(folderName + JAR_SEPARATOR)) {
                continue;
            }

            String fileName = entry.getName();

            if (fileName.charAt(fileName.length() - 1) == JAR_SEPARATOR) {
                File file = new File(destFolder + File.separator + fileName);
                if (file.isFile()) {
                    file.delete();
                }
                file.mkdirs();
                continue;
            }

            File file = new File(destFolder + File.separator + fileName);
            if (option == null && file.exists()) {
                continue;
            }

            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);

            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
        }

        zis.closeEntry();
        zis.close();
    }

    private static void copyFromDirectory(String folderName, File destFolder, CopyOption option, File fullPath)
        throws IOException {
        Files.walkFileTree(Paths.get(fullPath.getPath(), folderName), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                if (path.toFile().isFile()) {
                    Files.copy(path, Paths.get(destFolder.getPath(), path.toFile().getName()), option);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @FunctionalInterface
    public interface PathTrimmer {
        String trim(String original);
    }
}
