package restAPI.simulation_running;
import com.google.common.collect.Multimap;
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


import java.nio.file.Path;


@RestController
public class SimulationRunningController {

    private final SimulationRunningService simulationRunningService;

    Logger logger = LoggerFactory.getLogger(SimulationRunningController.class);

    @Autowired
    public SimulationRunningController(SimulationRunningService simulationRunningService) {
        this.simulationRunningService = simulationRunningService;
    }

    //For uploading the Multipart files and saving them to the file system. And then we run the simulation on them.
    @PostMapping("/simulate/upload")
    public ResponseEntity<String> handleMultipleFilesUpload(@RequestParam("files") MultipartFile[] files,
                                                            @RequestParam("simulation_id") String id) {
        try {
            Path tmpFolder = TempFileUtils.createDefaultTempDir("misim-");
            Path outputFolder = TempFileUtils.createDefaultTempDir("misim-output-");
            Multimap<String, String> savedFiles = TempFileUtils.saveFiles(files, tmpFolder);
            simulationRunningService.runExperiment(savedFiles, outputFolder);
            // TODO: Add DB connections

            // Do the clean-up
            //FileUtils.deleteDirectory(tmpFolder.toFile());
            //FileUtils.deleteDirectory(outputFolder.toFile());
            return new ResponseEntity<>("Files have been successfully uploaded, and the simulation is running.",
                    HttpStatus.OK);
        }
        catch (Exception e) {
            String errorMessage = e.getMessage();
            logger.error(errorMessage);
            return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}