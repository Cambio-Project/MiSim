package cambio.simulator;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration that controls how an experiment is started.
 *
 * @author Lion Wagner
 */
public class ExperimentStartupConfig {


    @CLIOption(
        opt = "a",
        longOpt = "arch_desc",
        description = "File path to an architectural description.",
        hasArg = true,
        required = true)
    private final String archDescLoc;

    @CLIOption(
        opt = "e",
        longOpt = "exp_desc",
        description = "File path to an experiment description.",
        hasArg = true,
        optionGroup = "experiment",
        optionGroupRequired = true)
    private final String expDescLoc;

    @CLIOption(
        opt = "s",
        longOpt = "scenario_desc",
        description = "File path to a scenario description.",
        hasArg = true,
        optionGroup = "experiment",
        optionGroupRequired = true)
    private final String scenario;

    @CLIOption(
        opt = "o",
        longOpt = "out",
        description = "Report Location Directory. Creates a new directory with "
            + "experiment name and start timestamp for each experiment run.",
        hasArg = true,
        optionGroup = "output")
    private final String reportLocation;

    @CLIOption(
        opt = "O",
        longOpt = "overwrite_out",
        description = "Report Location Directory. Is cleared of data before the experiment ",
        hasArg = true,
        optionGroup = "output")
    private final String reportOverwriteLocation;

    @CLIOption(
        opt = "p",
        longOpt = "progress_bar",
        description = "Show progressbar window during simulation. When "
            + "setting this flag, the simulator does not run in headless mode anymore.")
    private final boolean showProgressBar;

    @CLIOption(
        opt = "d",
        longOpt = "debug",
        description = "Turns on debug output of the simulator.")
    private final boolean debug;

    @CLIOption(
        opt = "t",
        longOpt = "no_traces",
        description = "Turns off the DESMO-J trace output of the simulator.")
    private final boolean noTraces;


    /**
     * Creates a new {@link ExperimentStartupConfig}.
     *
     * <p>
     * An expDescLoc or scenario have to be given. If an expDescLoc is given the scenario will be ignored. Boolean
     * default values are always {@code false}.
     *
     * @param archDescLoc             mandatory path to an architecture description
     * @param expDescLoc              path to an experiment description
     * @param scenario                path to a scenario description
     * @param reportLocation          directory path ot
     * @param reportOverwriteLocation
     * @param showProgressBar         when this option is set to true, a progressbar window is shown during the
     *                                simulation (setting this option disables headless mode and requires a display
     *                                output)
     * @param debug                   enables debug output
     */
    public ExperimentStartupConfig(@NotNull String archDescLoc, String expDescLoc, String scenario,
                                   String reportLocation,
                                   String reportOverwriteLocation, boolean showProgressBar, boolean debug,
                                   boolean traces) {
        this.archDescLoc = archDescLoc;
        this.expDescLoc = expDescLoc;
        this.scenario = scenario;
        this.reportLocation = reportLocation;
        this.reportOverwriteLocation = reportOverwriteLocation;
        this.showProgressBar = showProgressBar;
        this.debug = debug;
        this.noTraces = !traces;

        if (expDescLoc == null && scenario == null) {
            throw new RuntimeException("Either a experiment description location or scenario description "
                + "location have to be given.");
        }
    }


    public String getArchitectureDescLoc() {
        return archDescLoc;
    }

    public String getExperimentDescLoc() {
        return expDescLoc;
    }

    public String getScenario() {
        return scenario;
    }

    public String getReportLocation() {
        return reportLocation != null ? reportLocation : reportOverwriteLocation;
    }

    public boolean isOverwriteReportPath() {
        return reportOverwriteLocation != null;
    }

    public boolean showProgressBarOn() {
        return showProgressBar;
    }

    public boolean debugOutputOn() {
        return debug;
    }

    public boolean traceEnabled() {
        return !noTraces;
    }

}
