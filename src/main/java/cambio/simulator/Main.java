package cambio.simulator;

import java.util.Arrays;

import cambio.simulator.misc.RNGStorage;
import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
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
     * Exit codes for the program. See {@link #main(String[])} for meanings.
     */
    public static final class ExitCodes {
        public static final int SUCCESSFUL_RUN = 0;
        public static final int EXCEPTION_DURING_ARGUMENT_PARSING = 1;
        public static final int EXCEPTION_DURING_PARSING = 2;
        public static final int EXCEPTION_DURING_SIMULATION = 16;
        public static final int EXCEPTION_UNKNOWN = 512;
    }

    /**
     * Main entry point of the program. Pass "-h" to see arguments.
     *
     * <p>
     * This method will <b>always</b> call {@link System#exit(int)}! Be aware of that if you call it from other code. If
     * you want to avoid this behavior, consider calling {@link #runExperiment(String[])} or
     * {@link #runExperiment(ExperimentStartupConfig)} instead.
     *
     * <p>
     * Exit code meanings are as follows:
     *
     * <table>
     *     <caption>Exit codes.</caption>
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
     *        <td>Exception during parsing of models.</td>
     *     </tr>
     *     <tr>
     *        <td>16</td>
     *        <td>Exception during running of experiment.</td>
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
        int returnCode = runExperiment(args);

        System.exit(returnCode);
    }

    /**
     * Varargs variant of {@link #main(String[])}.
     *
     *
     * <p>
     * This method will <b>always</b> call {@link System#exit(int)}! Be aware of that if you call it from other code. If
     * you want to avoid this behavior, consider calling {@link #runExperiment(String[])} or
     * {@link #runExperiment(ExperimentStartupConfig)} instead.
     *
     * <p>
     * For exit code meanings, see {@link #main(String[])}.
     *
     * @param args program options, see {@link ExperimentStartupConfig}
     * @see #main(String[])
     * @see #runExperiment(String)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static void mainVarargs(final String... args) {
        main(args);
    }


    private static @NotNull ExperimentStartupConfig parseArgsToConfig(String[] args) throws ParseException {
        // trim whitespaces from arguments to please apache cli
        String[] argsTrimmed = Arrays.stream(args).map(String::trim).toArray(String[]::new);
        return CLI.parseArguments(ExperimentStartupConfig.class, argsTrimmed);
    }

    /**
     * Starts an experiment, and uses the given string as cli arguments (splits on spaces). Use spaces only to separate
     * arguments and not inside a value.
     *
     * @param cliString the cli argument string to parse. See {@link ExperimentStartupConfig} or --help for details.
     * @return the exit code of the experiment, see {@link #main(String[])} for meanings.
     * @see #main(String[])
     * @see #mainVarargs(String...)
     * @see #runExperiment(ExperimentStartupConfig)
     */
    public static int runExperiment(final String cliString) {
        return runExperiment(cliString.replaceAll("\\s*", " ").split(" "));
    }


    /**
     * Starts an experiment, and uses the given string as cli arguments. See {@link ExperimentStartupConfig} or --help
     * for details.
     *
     * @param args the cli arguments to parse. See {@link ExperimentStartupConfig} for options.
     * @return the exit code of the experiment, see {@link #main(String[])} for meanings.
     */
    public static int runExperiment(String[] args) {
        ExperimentStartupConfig startupConfig;

        try {
            startupConfig = parseArgsToConfig(args);
        } catch (ParseException e) {
            System.err.println("[ERROR] " + e.getMessage());
            return ExitCodes.EXCEPTION_DURING_ARGUMENT_PARSING;
        }
        return runExperiment(startupConfig);
    }


    /**
     * Starts an experiment with the given {@link ExperimentStartupConfig}.
     *
     * @param startupConfig the experiment startup configuration
     * @return the exit code of the experiment, see {@link #main(String[])} for meanings.
     * @see #runExperiment(String)
     * @see #main(String[])
     * @see #mainVarargs(String...)
     */
    public static int runExperiment(final ExperimentStartupConfig startupConfig) {
        boolean isDebugOutputOn = startupConfig.debugOutputOn();
        try {

            Experiment experiment = new ExperimentCreator().createSimulationExperiment(startupConfig);
            System.out.printf("[INFO] Starting simulation at approximately %s%n", java.time.LocalDateTime.now());
            experiment.start();
            experiment.finish();

            RNGStorage.reset(); //TODO: this should happen first


            if (experiment.hasError()) {
                System.out.println("[INFO] Simulation failed.");
                return ExitCodes.EXCEPTION_DURING_SIMULATION;
            }

            System.out.println("[INFO] Simulation finished successfully.");
            writeCommandLineReport((MiSimModel) experiment.getModel());
            return ExitCodes.SUCCESSFUL_RUN;

        } catch (ParsingException | JsonParseException e) {
            Util.printExceptionMessage(e, isDebugOutputOn);
            return ExitCodes.EXCEPTION_DURING_PARSING;
        } catch (Exception e) {
            Util.printExceptionMessage(e, isDebugOutputOn);
            return ExitCodes.EXCEPTION_UNKNOWN;
        }
    }

    private static void writeCommandLineReport(MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        System.out.println("\n*** MiSim Report ***");
        System.out.println("Simulation of Architecture: "
                           + metaData.getArchitectureDescriptionLocation().getAbsolutePath());
        System.out.println("Executed Experiment:        "
                           + metaData.getExperimentDescriptionLocation().getAbsolutePath());
        System.out.println("Report Location:            "
                           + metaData.getReportLocation().toAbsolutePath());
        System.out.println("Setup took:                 " + Util.timeFormat(metaData.getSetupExecutionDuration()));
        System.out.println("Experiment took:            " + Util.timeFormat(metaData.getExperimentExecutionDuration()));
        System.out.println("Execution took:             " + Util.timeFormat(metaData.getExecutionDuration()));
    }
}
