package restAPI.simulation_running;

import cambio.simulator.ExperimentCreator;
import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.misc.RNGStorage;
import cambio.simulator.parsing.ParsingException;
import com.google.common.collect.Multimap;
import com.google.gson.JsonParseException;
import desmoj.core.simulator.Experiment;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Service
public class SimulationRunningService {
    private static final boolean isWindows = System.getProperty("os.name").startsWith("Windows");

    public void runExperiment(Multimap<String, String> inputFiles, Path outPutDir) throws Exception {
        Collection<String> archDescPathCollection = inputFiles.get("architecture");
        Collection<String> expDescPathCollection = inputFiles.get("experiment");
        Collection<String> scenarioPathCollection = inputFiles.get("scenario");
        List<String> load = inputFiles.get("load").stream().toList();


        if (archDescPathCollection.isEmpty()) {
            throw new Exception("You have to provide an architecture description file.");
        } else if (expDescPathCollection.isEmpty() && scenarioPathCollection.isEmpty()) {
            throw new Exception("You have to provide either an experiment or a scenario description file.");
        }
        String expDescPath = null;
        String scenarioPath = null;

        String archDescPath = archDescPathCollection.iterator().next();
        if (expDescPathCollection.iterator().hasNext()) {
            expDescPath = expDescPathCollection.iterator().next();
            if(!load.isEmpty()) {
                adjustWorkloadPaths(load, expDescPath);
            }
        }
        if (scenarioPathCollection.iterator().hasNext()) {
            scenarioPath = scenarioPathCollection.iterator().next();
            if(!load.isEmpty()) {
                adjustWorkloadPaths(load, scenarioPath);
            }
        }

        ExperimentStartupConfig config = new ExperimentStartupConfig(archDescPath, expDescPath,
                scenarioPath,null, outPutDir.toString(), false,
                false, true);
        try {
            Experiment experiment = new ExperimentCreator().createSimulationExperiment(config);
            experiment.start();
            experiment.finish();
            // TODO check whether we need to reset the generator;
            RNGStorage.reset();
        } catch (ParsingException | JsonParseException e) {
            if (config.debugOutputOn()) {
                e.printStackTrace();
            } else {
                throw new Exception(e.getMessage());
            }
        }
    }


    private void adjustWorkloadPaths(List<String> workloadsPath, String experimentFilePath)
            throws IOException {
        Path experimentFile = Path.of(FilenameUtils.separatorsToSystem(experimentFilePath));
        String content = Files.readString(experimentFile, StandardCharsets.UTF_8);

        String workloadFileName;
        for(String path : workloadsPath) {
            workloadFileName = FilenameUtils.getName(path);
            content = content.replace(workloadFileName, isWindows ? path.replace("\\","\\\\") : path);
        }
        Files.write(experimentFile, content.getBytes());
    }

}
