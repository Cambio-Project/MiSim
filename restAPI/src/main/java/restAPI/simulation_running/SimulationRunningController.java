package restAPI.simulation_running;
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
import restAPI.util.ReportDataPointsManipulator;
import restAPI.util.TempFileUtils;


import java.io.IOException;
import java.nio.file.Files;
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
                                                            @RequestParam("simulation_id") String id) throws IOException {
        return runSimulation(files, id);
    }

    @PostMapping("/simulate/upload/mtl")
    public ResponseEntity<String> handleMTLSpecification(@RequestParam("files") MultipartFile[] files,
                                                         @RequestParam("simulation_id") String id) throws IOException {
        return runSimulation(files, id);
    }

    // TODO: Handle this call in a non-blocking manner, taking into account that this implementation is not
    //  client friendly as it can time-out the request due to the long processing time.
    private ResponseEntity<String> runSimulation(MultipartFile[] files,
                                                 String id) throws IOException  {
        try {
            if (TempFileUtils.existsSimulationId(id)) {
                return new ResponseEntity<>(String.format("Simulation ID <%s> is already in use. " +
                        "Please provide a unique new id.", id),
                        HttpStatus.BAD_REQUEST);
            }
            Path tmpFolder = TempFileUtils.createDefaultTempDir("misim-");
            Path outputFolder = TempFileUtils.createOutputDir(TempFileUtils.RAW_OUTPUT_DIR, id);

            System.out.println("The output file already exits: " + Files.exists(outputFolder));

            Multimap<String, String> savedFiles = TempFileUtils.saveFiles(files, tmpFolder);
            //Block1
            simulationRunningService.runExperiment(savedFiles, outputFolder);
            if(!TempFileUtils.existsSimulationId(id)) {
                return new ResponseEntity<>(String.format("An Error happened when running the simulation with the ID: " +
                        "%s.", id),
                        HttpStatus.INTERNAL_SERVER_ERROR);
            }
            String rawResultsDirPath = outputFolder.toString() + TempFileUtils.SEPARATOR + "raw";
            //Block2
            ReportDataPointsManipulator.adjustSimulationResults(rawResultsDirPath, id);
            // Do the clean-up
            FileUtils.deleteDirectory(tmpFolder.toFile());
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