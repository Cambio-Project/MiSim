package cambio.simulator;

import desmoj.core.simulator.Experiment;
import org.apache.commons.cli.CommandLine;
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
        CommandLine cl = null;

        try {
            cl = CLI.parseArguments(args);
        } catch (ParseException e) {
            System.exit(1);
        }

        ExperimentStartupConfig config = ExperimentStartupConfig.fromCL(cl);
        Experiment experiment = ExperimentCreator.createSimulationExperiment(config);
        experiment.start();
        experiment.finish();
    }

}