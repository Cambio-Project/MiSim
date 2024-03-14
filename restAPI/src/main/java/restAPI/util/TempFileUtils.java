package restAPI.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

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
        if(file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("The uploaded file must have a name that includes the prefix" +
                    " <architecture_>, <experiment_>, <scenario_>, <mtl_>, or <load_> according to its type.");
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if(fileName.contains("..")
                    || !(fileName.startsWith("architecture_") || fileName.startsWith("experiment_")
                    || fileName.startsWith("scenario_") || fileName.startsWith("load_") ||
                    fileName.startsWith("mtl_"))) {
                throw new Exception("Filename contains invalid path sequence: " + fileName);
            } else if (file.isEmpty()) {
                throw new Exception(String.format("The uploaded file <%s> is empty.", fileName));
            }
            byte[] content = file.getBytes();
            return createFile(path, fileName, content);
        } catch (MaxUploadSizeExceededException e) {
            throw new MaxUploadSizeExceededException(file.getSize());
        }
    }

    public static Multimap<String, String> saveFiles(MultipartFile[] files, Path temDir) {
        Multimap<String, String> filesPaths = ArrayListMultimap.create();
        Arrays.asList(files).forEach(file -> {
            try {
                Path tmpFile = saveFile(file, temDir);
                String filePath = tmpFile.toString();
                String fileName = tmpFile.getFileName().toString();
                if (fileName.startsWith("architecture_")) {
                    filesPaths.put("architecture", filePath);
                } else if (fileName.startsWith("experiment_")) {
                    filesPaths.put("experiment", filePath);
                } else if (fileName.startsWith("scenario_")) {
                    filesPaths.put("scenario", filePath);
                } else if (fileName.startsWith("mtl_")) {
                    filesPaths.put("mtl", filePath);
                }
                else if (fileName.startsWith("load_")){
                    filesPaths.put("load", filePath);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        System.out.println(filesPaths);
        return filesPaths;
    }

    // Create a temp directory in the default OS's /tmp folder.
    public static Path createDefaultTempDir(String prefix) throws IOException {
        return Files.createTempDirectory(prefix);
    }

    public static Path createOutputDir(String outputDirName, String simulationId) throws IOException {
        Path outPutDirPath = Path.of(outputDirName);
        if (!Files.exists(outPutDirPath)) {
            Files.createDirectory(outPutDirPath);
        }
        String simulationOutputDirPath = outputDirName + SEPARATOR + simulationId;
        return Files.createDirectory(Path.of(simulationOutputDirPath));
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

    public static boolean existsSimulationId(String simulationId) {
        String simulationOutputDirPath = RAW_OUTPUT_DIR + SEPARATOR + simulationId;
        return Files.exists(Path.of(simulationOutputDirPath));
    }
}
