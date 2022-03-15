package cambio.simulator;

import org.jetbrains.annotations.NotNull;

/**
 * Configuration that controls how an experiment is started.
 *
 * @author Lion Wagner
 */
public final class ExperimentStartupConfig {

    @CLIOption(
        opt = "a",
        longOpt = "arch_desc",
        description = "file path to an architectural description",
        hasArg = true,
        required = true)
    private final String archDescLoc;

    @CLIOption(
        opt = "e",
        longOpt = "exp_desc",
        description = "file path to an experiment description",
        hasArg = true,
        optionGroup = "experiment",
        optionGroupRequired = true)
    private final String expDescLoc;

    @CLIOption(
        opt = "s",
        longOpt = "scenario_desc",
        description = "file path to a scenario description",
        hasArg = true,
        optionGroup = "experiment",
        optionGroupRequired = true)
    private final String scenario;

    @CLIOption(
        opt = "o",
        longOpt = "out",
        description = "Report Location Directory. Creates a new directory with "
            + "experiment name and start timestamp for each experiment.",
        hasArg = true)
    private final String reportLocation;

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
    private final boolean debugOutput;

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
     * @param archDescLoc    mandatory path to an architecture description
     * @param expDescLoc     path to an experiment description
     * @param scenario        path to a scenario description
     * @param reportLocation  directory path ot
     * @param showProgressBar when this option is set to true, a progressbar window is shown during the simulation
     *                        (setting this option disables headless mode and requires a display output)
     * @param debugOutput     enables debug output
     */
    public ExperimentStartupConfig(@NotNull String archDescLoc, String expDescLoc, String scenario,
                                   String reportLocation,
                                   boolean showProgressBar, boolean debugOutput, boolean noTraces) {
        this.archDescLoc = archDescLoc;
        this.expDescLoc = expDescLoc;
        this.scenario = scenario;
        this.reportLocation = reportLocation;
        this.showProgressBar = showProgressBar;
        this.debugOutput = debugOutput;
        this.noTraces = noTraces;

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
        return reportLocation;
    }

    public boolean showProgressBarOn() {
        return showProgressBar;
    }

    public boolean debugOutputOn() {
        return debugOutput;
    }

    public boolean noTraces() {
        return noTraces;
    }
}
