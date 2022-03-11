package cambio.simulator;

import cambio.simulator.export.ReportCollector;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Experiment;
import org.apache.commons.cli.ParseException;

/**
 * Main class of the simulator. Takes care of triggering cli parsing, experiment creation and running.
 *
 * @author Lion Wagner
 */
public final class Main {

    /**
     * Main entry point of the program.
     *
     * @param args program options, see {@link CLI}
     */
    public static void main(String[] args) {

        ExperimentStartupConfig startupConfig = null;

        try {
            startupConfig = CLI.parseArguments(ExperimentStartupConfig.class, args);
        } catch (ParseException e) {
            System.exit(1);
        }

        startExperiment(startupConfig);
    }

    public static void startExperiment(String cliString) {
        main(cliString.split(" "));
    }

    public static void startExperiment(ExperimentStartupConfig startupConfig) {
        Experiment experiment = ExperimentCreator.createSimulationExperiment(startupConfig);
        System.out.printf("[INFO] Starting simulation at approximately %s%n", java.time.LocalDateTime.now());
        experiment.start();
        experiment.finish();
        if (experiment.isAborted()) {
            System.out.println("[INFO] Simulation failed.");
        } else {
            System.out.println("[INFO] Simulation finished successfully.");
        }
        ReportCollector.getInstance().printReport((MiSimModel) experiment.getModel());
    }
}