package restAPI.util;

import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TempFileUtils {

    public static final String RAW_OUTPUT_DIR = "raw_simulations_results";
    public static final String OUTPUT_DIR = "simulations_results";
    public static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    // Create a new file and write the given content to it.
    private static Path createFile(Path tmpDir, String originalName, byte[] content) throws IOException {
        String filePath = tmpDir.toString() + SEPARATOR + originalName;
        Path file = Files.createFile(Path.of(filePath));
        return Files.write(file, content);
    }


    private static Path saveFile(MultipartFile file, Path path) throws Exception {
        if (file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("The uploaded file must have a name.");
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (fileName.contains("..")) {
                throw new Exception("Filename contains invalid path sequence: " + fileName);
            }
            byte[] content = file.getBytes();
            return createFile(path, fileName, content);
        } catch (MaxUploadSizeExceededException e) {
            throw new MaxUploadSizeExceededException(file.getSize());
        }
    }

    public static Multimap<String, String> saveFile(Multimap<String, String> filesPaths, String type,
                                                    MultipartFile[] files,
                                                    Path temDir) {
        if (files != null) {
            Arrays.asList(files).forEach(file -> {
                try {
                    Path tmpFile = saveFile(file, temDir);
                    String filePath = tmpFile.toString();
                    filesPaths.put(type, filePath);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return filesPaths;
    }

    // Create a temp directory in the default OS's /tmp folder.
    public static Path createDefaultTempDir(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    public static Path createOutputDir(String outputDirName, String simulationId, String executionId)
        throws IOException {
        Path outPutDirPath = Path.of(outputDirName);
        if (!Files.exists(outPutDirPath)) {
            Files.createDirectory(outPutDirPath);
        }
        Path simulationOutputDirPath = Path.of(outputDirName + SEPARATOR + simulationId);
        if (!Files.exists(simulationOutputDirPath)) {
            Files.createDirectory(simulationOutputDirPath);
        }
        String executionOutputDirPath = simulationOutputDirPath + SEPARATOR + executionId;
        return Files.createDirectory(Path.of(executionOutputDirPath));
    }


    public static Set<String> getFilesFromResultsDir(Path dirPath) throws IOException {
        String dir = dirPath.toString();
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toSet());
        }
    }

    public static boolean existsSimulationId(String simulationId, String executionId) {
        String simulationOutputDirPath = RAW_OUTPUT_DIR + SEPARATOR + simulationId + SEPARATOR + executionId;
        return Files.exists(Path.of(simulationOutputDirPath));
    }

    public static void cleanOutputDir(String simulationId) {
        String simulationOutputDirPath = RAW_OUTPUT_DIR + SEPARATOR + simulationId;
        try {
            FileUtils.deleteDirectory(new File(simulationOutputDirPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
