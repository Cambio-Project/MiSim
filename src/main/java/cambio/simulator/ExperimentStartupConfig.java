package cambio.simulator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.jetbrains.annotations.NotNull;

/**
 * Configuration that controls how an experiment is started.
 *
 * @author Lion Wagner
 */
public final class ExperimentStartupConfig {

    private String archDesc;
    private String expDesc;
    private String scenario;
    private String reportLocation;

    private boolean showProgressBar;
    private boolean debugOutput;

    private ExperimentStartupConfig() {

    }

    /**
     * Creates a new {@link ExperimentStartupConfig}.
     *
     * <p>
     * An expDesc or scenario have to be given. If an expDesc is given the scenario will be ignored.
     * Boolean default values are always {@code false}.
     *
     * @param archDesc mandatory path to an architecture description
     * @param expDesc path to an experiment description
     * @param scenario path to a scenario description
     * @param reportLocation directory path ot
     * @param showProgressBar when this option is set to true, a progressbar window is shown during the simulation
     *                        (setting this option disables headless mode and requires a display output)
     * @param debugOutput enables debug output
     */
    public ExperimentStartupConfig(@NotNull String archDesc, String expDesc, String scenario, String reportLocation,
                                   boolean showProgressBar, boolean debugOutput) {
        this.archDesc = archDesc;
        this.expDesc = expDesc;
        this.scenario = scenario;
        this.reportLocation = reportLocation;
        this.showProgressBar = showProgressBar;
        this.debugOutput = debugOutput;

        if (expDesc == null && scenario == null) {
            throw new RuntimeException("Either a experiment description location or scenario description "
                + "location have to be given.");
        }
    }


    /**
     * Creates a new {@link ExperimentStartupConfig} based on the given {@link CommandLine} that should be created
     * via the {@link CLI} class.
     *
     * @param cl {@link CommandLine} that contains the parsed cli options.
     * @return a new parsed {@link ExperimentStartupConfig}
     */
    public static ExperimentStartupConfig fromCL(CommandLine cl) {
        ExperimentStartupConfig config = new ExperimentStartupConfig();

        Map<String, Field> nameMapConfig = new HashMap<>();

        for (Field configField : ExperimentStartupConfig.class.getDeclaredFields()) {
            nameMapConfig.put(configField.getName(), configField);
        }

        for (Field optionField : CLI.class.getDeclaredFields()) {
            if (optionField.getName().endsWith("Opt") && Option.class.isAssignableFrom(optionField.getType())) {
                try {
                    String name = optionField.getName().substring(0, optionField.getName().length() - 3);
                    Option opt = (Option) optionField.get(null);
                    Option resolvedOption =
                        Arrays.stream(cl.getOptions())
                            .filter(
                                option -> Objects.equals(option.getOpt(), opt.getOpt())
                                    || Objects.equals(option.getLongOpt(), opt.getLongOpt()))
                            .findFirst()
                            .orElse(null);

                    Field targetField = nameMapConfig.get(name);
                    if (targetField == null || resolvedOption == null) {
                        continue;
                    } else if (boolean.class.isAssignableFrom(targetField.getType())) {
                        targetField.set(config, true);
                    } else if (String.class.isAssignableFrom(targetField.getType())) {
                        String value = resolvedOption.getValue();
                        targetField.set(config, value);
                    } else if (String[].class.isAssignableFrom(targetField.getType())) {
                        String[] values = resolvedOption.getValues();
                        targetField.set(config, values);
                    } else {
                        throw new ClassCastException("Can only parse CLI options to the types of boolean, String or "
                            + "String[].");
                    }

                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return config;
    }


    public String getArchitectureDescLoc() {
        return archDesc;
    }

    public String getExperimentDescLoc() {
        return expDesc;
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
}
