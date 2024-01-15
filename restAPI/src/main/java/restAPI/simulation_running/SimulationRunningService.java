package restAPI.simulation_running;

import cambio.simulator.ExperimentCreator;
import cambio.simulator.ExperimentStartupConfig;
import cambio.simulator.misc.RNGStorage;
import desmoj.core.simulator.Experiment;

import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SimulationRunningService {

    public void runExperiment(HashMap<String, String> inputFiles, Path outPutDir) {
        String archDescPath = inputFiles.get("architecture");
        String expDescPath = inputFiles.get("experiment");
        String scenarioPath = inputFiles.get("scenario");

        ExperimentCreator creator = new ExperimentCreator();
        ExperimentStartupConfig config = new ExperimentStartupConfig(archDescPath, expDescPath,
                scenarioPath, outPutDir.toString(), null, false,
                false, true);

        Experiment experiment = creator.createSimulationExperiment(config);
        experiment.start();
        experiment.finish();

        // TODO check whether we need to reset the generator;
        RNGStorage.reset();
    }



}
