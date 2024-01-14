package restAPI.simulation_running;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import restAPI.util.TempFileUtil;
import java.nio.file.Path;
import java.io.IOException;

@RestController
public class SimulationRunningController {

    private final SimulationRunningService simulationRunningService;

    Logger logger = LoggerFactory.getLogger(SimulationRunningController.class);

    @Autowired
    public SimulationRunningController(SimulationRunningService simulationRunningService) {
        this.simulationRunningService = simulationRunningService;
    }

    @PostMapping("/simulate")
    public ResponseEntity<String> handleSimulationRequest(@RequestBody String request) {

        return new ResponseEntity<>("Im a response", HttpStatus.OK);
    }

    @PostMapping("/simulate/upload")
    public ResponseEntity<String> fileUploading(@RequestParam("file") MultipartFile file) {
        // Code to save the file to a database or disk
        return ResponseEntity.ok("Successfully uploaded the file");
    }


    //for uploading the MULTIPLE file to the File system
    @PostMapping("/multiple/file")
    public ResponseEntity<String> handleMultipleFilesUpload(@RequestParam("files") MultipartFile[] files)
            throws IOException {
        try {
            Path tmpFolder = TempFileUtil.createDefaultTempDir();
            Path[] savedFiles = TempFileUtil.saveFiles(files, tmpFolder);
            return ResponseEntity.ok("Successfully uploaded the file");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}