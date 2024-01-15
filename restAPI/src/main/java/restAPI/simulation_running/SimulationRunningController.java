package restAPI.simulation_running;
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
import restAPI.util.TempFileUtil;


import java.nio.file.Path;
import java.io.IOException;
import java.util.HashMap;


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
    public ResponseEntity<String> handleMultipleFilesUpload(@RequestParam("files") MultipartFile[] files) {
        try {
            Path tmpFolder = TempFileUtil.createDefaultTempDir("misim-");
            Path outputFolder = TempFileUtil.createDefaultTempDir("misim-output-");
            HashMap<String, String> savedFiles = TempFileUtil.saveFiles(files, tmpFolder);
            simulationRunningService.runExperiment(savedFiles, outputFolder);
            // TODO: Add DB connections

            // Do the clean-up
            // TODO delete the created output files after saving to DB.
            //FileUtils.deleteDirectory(tmpFolder.toFile());
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