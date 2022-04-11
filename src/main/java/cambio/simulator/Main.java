package cambio.simulator;

import java.util.Arrays;

import cambio.simulator.export.ReportCollector;
import cambio.simulator.misc.RNGMananger;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.parsing.ParsingException;
import com.google.gson.JsonParseException;
import desmoj.core.simulator.Experiment;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;

/**
 * Main class of the simulator. Takes care of triggering cli parsing, experiment creation and running.
 *
 * @author Lion Wagner
 */
public final class Main {

    /**
     * Main entry point of the program. Pass "-h" to see arguments.
     *
     * <table>
     *     <tr>
     *         <th>Exit Code</th>
     *         <th>Description</th>
     *        </tr>
     *     <tr>
     *        <td>0</td>
     *        <td>Success</td>
     *     </tr>
     *     <tr>
     *        <td>1</td>
     *        <td>Invalid arguments</td>
     *     </tr>
     *     <tr>
     *        <td>2</td>
     *        <td>Exception during parsing.</td>
     *     </tr>
     *     <tr>
     *        <td>16</td>
     *        <td>Exception during running.</td>
     *     </tr>
     *     <tr>
     *         <td>512</td>
     *         <td>Unexpected or unknown exception occurred.</td>
     *     </tr>
     * </table>
     *
     * @param args program options, see {@link ExperimentStartupConfig}
     * @see #mainVarargs(String...)
     * @see #runExperiment(String)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void main(final String[] args) {

        // trim whitespaces from arguments to please apache cli
        String[] argsTrimmed = Arrays.stream(args).map(String::trim).toArray(String[]::new);

        ExperimentStartupConfig startupConfig = parseArgsToConfig(argsTrimmed);

        try {

            Experiment experiment = runExperiment(startupConfig);

            ReportCollector.getInstance().printReport((MiSimModel) experiment.getModel());


            //-------------------------------------------Error handling-------------------------------------------------

            if (experiment.hasError()) {
                System.out.println("[INFO] Simulation failed.");
                System.exit(16);
            } else {
                System.out.println("[INFO] Simulation finished successfully.");
                System.exit(0);
            }
        } catch (ParsingException | JsonParseException e) {
            if (startupConfig.debugOutputOn()) {
                e.printStackTrace();
            } else {
                System.out.println("[ERROR] " + e.getMessage());
            }
            System.exit(2);
        } catch (Exception e) {
            //In tests, System.exit throws an exception with a private type from the
            //"com.github.stefanbirkner.systemlambda" package. This exception is supposed to be
            //thrown up to top level and therefore is not handled here.
            if (e.getClass().getPackage().getName().equals("com.github.stefanbirkner.systemlambda")) {
                throw e;
            }

            if (startupConfig.debugOutputOn()) {
                e.printStackTrace();
            }

            System.exit(512);
        }
    }

    @NotNull
    private static ExperimentStartupConfig parseArgsToConfig(String[] argsTrimmed) {
        try {
            return CLI.parseArguments(ExperimentStartupConfig.class, argsTrimmed);
        } catch (ParseException e) {
            System.err.println("[ERROR] " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    /**
     * Varargs variant of {@link #main(String[])}.
     *
     * @param args program options, see {@link ExperimentStartupConfig}
     * @see #main(String[])
     * @see #runExperiment(String)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void mainVarargs(final String... args) {
        main(args);
    }

    /**
     * Starts an experiment, and uses the given string as cli arguments (splits on spaces). Use spaces only to separate
     * arguments and not inside a value.
     *
     * @param cliString the cli argument string
     * @see #main(String[])
     * @see #mainVarargs(String...)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void runExperiment(final String cliString) {
        main(cliString.replaceAll("\\s*", " ").split(" "));
    }


    /**
     * Starts an experiment with the given {@link ExperimentStartupConfig}.
     *
     * @param startupConfig the experiment startup configuration
     * @see #runExperiment(String)
     * @see #main(String[])
     * @see #mainVarargs(String...)
     */
    public static Experiment runExperiment(final ExperimentStartupConfig startupConfig) {
        Experiment experiment = ExperimentCreator.createSimulationExperiment(startupConfig);
        System.out.printf("[INFO] Starting simulation at approximately %s%n", java.time.LocalDateTime.now());
        experiment.start();
        experiment.finish();

        RNGMananger.reset();

        return experiment;
    }
}