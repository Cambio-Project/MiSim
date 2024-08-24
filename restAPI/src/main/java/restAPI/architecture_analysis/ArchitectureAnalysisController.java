package restAPI.architecture_analysis;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import restAPI.util.TempFileUtils;

import java.io.IOException;
import java.nio.file.Path;

@RestController
public class ArchitectureAnalysisController {
    public static final String architectureFieldName = "architecture";
    private final ArchitectureAnalysisService architectureAnalysisService;

    Logger logger = LoggerFactory.getLogger(ArchitectureAnalysisController.class);

    @Autowired
    public ArchitectureAnalysisController(ArchitectureAnalysisService architectureAnalysisService) {
        this.architectureAnalysisService = architectureAnalysisService;
    }

    @PostMapping("/analyze/upload")
    public ResponseEntity<String> handleMultipleFilesUpload(
            @RequestParam("architectures") MultipartFile[] architectures) throws IOException {
        return runSimulation(architectures);
    }

    // TODO: Handle this call in a non-blocking manner, taking into account that this implementation is not
    //  client friendly as it can time-out the request due to the long processing time.
    private ResponseEntity<String> runSimulation(MultipartFile[] architectures) throws IOException {
        Path tmpFolder = null;
        try {
            tmpFolder = TempFileUtils.createDefaultTempDir("architecture-analysis");
            Multimap<String, String> savedFiles = saveArchitectureFile(architectures, tmpFolder);
            var response = architectureAnalysisService.runAnalysis(savedFiles);
            return new ResponseEntity<>(response.toJSON(), HttpStatus.OK);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            logger.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            // Do the clean-up
            if (tmpFolder != null) {
                FileUtils.deleteDirectory(tmpFolder.toFile());
            }
        }
    }

    private Multimap<String, String> saveArchitectureFile(MultipartFile[] architectures, Path tmpFolder) {
        Multimap<String, String> savedFiles = ArrayListMultimap.create();
        savedFiles = TempFileUtils.saveFile(savedFiles, architectureFieldName, architectures, tmpFolder);
        return savedFiles;
    }

}