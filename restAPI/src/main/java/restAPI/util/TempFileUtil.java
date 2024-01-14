package restAPI.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;


public class TempFileUtil {

    // Create a new temp file.
    private static Path createTempFile(Path tmpDir, byte[] content, String prefix) throws IOException {
        String customFileSuffix = ".json";

        Path tmpFile = Files.createTempFile(tmpDir, prefix, customFileSuffix);
        return Files.write(tmpFile, content);
    }

    // TODO: check whether the uploaded file is a json file: String contentType = file.getContentType();
    // TODO: check the file name for the prefixes.
    private static Path saveFile(MultipartFile file, Path path) throws Exception {
        if(file.getOriginalFilename() == null) {
            throw new IllegalArgumentException("The uploaded file must have a name that includes the prefix" +
                    " <architecture_> or <experiment_>.");
        }
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if(fileName.contains("..")
                    || !fileName.startsWith("architecture_")
                    || !fileName.startsWith("experiment_") ) {
                throw new Exception("Filename contains invalid path sequence " + fileName);
            } else if (file.isEmpty()) {
                throw new Exception(String.format("The uploaded file <%s> is empty.", fileName));
            }
            byte[] content = file.getBytes();
            return createTempFile(path, content, fileName);
        } catch (MaxUploadSizeExceededException e) {
            throw new MaxUploadSizeExceededException(file.getSize());
        } catch (Exception e) {
            throw new Exception("Could not save File: " + fileName);
        }
    }
    public static Path[] saveFiles(MultipartFile[] files, Path temDir) {
        Path[] filesPaths = new Path[files.length];
        int i = 0;
        Arrays.asList(files).forEach(file -> {
            try {
                filesPaths[i] = saveFile(file, temDir);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        return filesPaths;
    }

    // We create a temp directory in the default OS's /tmp folder.
    public static Path createDefaultTempDir() throws IOException {
        return Files.createTempDirectory("misim");
    }

}
