package restAPI.simulation_running;

import cambio.simulator.ExperimentStartupConfig;
import desmoj.core.simulator.Experiment;
import org.springframework.stereotype.Service;
import restAPI.simulation_running.restufl_experiment_creation.RestfulExperimentCreator;

@Service
public class SimulationRunningService {


    public Experiment runExperiment(final ExperimentStartupConfig startupConfig) {
        Experiment experiment = new RestfulExperimentCreator().createSimulationExperiment(startupConfig);
        System.out.printf("[INFO] Starting simulation at approximately %s%n", java.time.LocalDateTime.now());
        experiment.start();
        experiment.finish();

        //RNGStorage.reset();

        return experiment;
    }
}
