package cambio.simulator.misc;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Utility class for checking file existence.
 *
 * @author Lion Wagner
 */
public final class FileUtilities {
    public static final char JAR_SEPARATOR = '/';

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

    /**
     * Copies a resource directory form the jar to  the given destination.
     *
     * @throws IOException if the target directory cannot be read properly.
     */
    public static void copyFolderFromResources(String jarFolderName, File destinationFolder) throws IOException {
        copyFolderFromResources(jarFolderName, destinationFolder, null);
    }


    /**
     * Copies a resource directory form the jar to  the given destination.
     *
     * @throws IOException if the target directory cannot be read properly.
     */
    public static void copyFolderFromResources(String folderName, File destFolder, CopyOption option)
        throws IOException {
        if (!destFolder.exists()) {
            boolean ignored = destFolder.mkdirs();
        }

        File fullPath = null;
        String path = FileUtilities.class.getProtectionDomain().getCodeSource().getLocation().getPath();

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

        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(fullPath)));

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            if (!entry.getName().startsWith(folderName + JAR_SEPARATOR)) {
                continue;
            }
            if (entry.isDirectory()) {
                continue;
            }

            String fileName = entry.getName().replace(folderName + JAR_SEPARATOR, "");
            Path targetFile = Paths.get(destFolder.getAbsolutePath(), fileName);
            Files.createDirectories(targetFile.getParent());
            if (Files.exists(targetFile) && option == StandardCopyOption.REPLACE_EXISTING) {
                Files.delete(targetFile);
            }
            Files.createFile(targetFile);


            try (FileOutputStream fos = new FileOutputStream(targetFile.toFile())) {
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
            }
        }

        zis.closeEntry();
        zis.close();
    }

    private static void copyFromDirectory(String folderName, File destFolder, CopyOption option, File fullPath)
        throws IOException {
        Files.walkFileTree(Paths.get(fullPath.getPath(), folderName), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                String sourceFullPath = path.toAbsolutePath().toString();
                String sourceDirectory = Paths.get(fullPath.getAbsolutePath(), folderName).toString();
                Path subdirAndFile = Paths.get(sourceFullPath.replace(sourceDirectory, ""));

                Path targetPath = Paths.get(destFolder.getPath(), subdirAndFile.toString());
                if (path.toFile().isFile()) {
                    Files.createDirectories(targetPath.getParent());
                    Files.copy(path, targetPath, option);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
