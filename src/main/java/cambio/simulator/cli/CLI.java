package cambio.simulator.cli;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Static class that holds the command line options of the simulator and is able to parse these into a {@link
 * CommandLine} object for retrieval.
 *
 * @author Lion Wagner
 */
public final class CLI {

    /**
     * Variable to collect all available program argument {@link Option}s.
     */
    private static final Options options = new Options();

    /**
     * Current parsed command line arguments.
     */
    private static CommandLine cl;


    public static final Option archModelOpt =
        Option.builder("a")
            .longOpt("arch_desc")
            .desc("file path to an architectural description")
            .hasArg()
            .required()
            .build();


    public static final Option expModelOpt =
        Option.builder("e")
            .longOpt("exp_desc")
            .desc("file path to an experiment description")
            .hasArg()
            .build();


    public static final Option scenarioOpt =
        Option.builder("s")
            .longOpt("scenario_desc")
            .desc("file path to a scenario description")
            .hasArg()
            .build();


    public static final Option showProgressBar =
        Option.builder("p")
            .longOpt("progress_bar")
            .desc("Show progressbar window during simulation. When setting this flag, the simulator does not run in "
                + "headless mode anymore.")
            .hasArg(false)
            .build();

    public static final Option debugOutput =
        Option.builder("d")
            .desc("Turns on debug output of the simulator.")
            .hasArg(false)
            .build();

    public static final Option reportLocation =
        Option.builder("o")
            .longOpt("--out")
            .desc("Report Location Directory. Creates a new directory with experiment name and start timestamp for "
                + "each experiment.")
            .hasArg()
            .build();


    static {

        //load all static Option fields into the options field
        for (Field declaredField : CLI.class.getDeclaredFields()) {
            if (Modifier.isStatic(declaredField.getModifiers())
                && Option.class.isAssignableFrom(declaredField.getType())) {
                try {
                    options.addOption((Option) declaredField.get(null));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        //mutual exclusion of scenario and experiment
        OptionGroup group = new OptionGroup()
            .addOption(scenarioOpt)
            .addOption(expModelOpt);
        group.setRequired(true);
        options.addOptionGroup(group);
    }


    /**
     * Parses the given arguments into a {@link CommandLine} object.
     *
     * <p>
     * Prints an Error and the Help overview automatically on {@link System#out} if anything with the options is
     * faulty.
     *
     * @param args program arguments
     * @return an instance of {@link CommandLine} that represents the parsed arguments.
     * @throws ParseException when something goes wrong during parsing (e.g. missing required options or arguments)
     */
    public static CommandLine parseArguments(final String[] args) throws ParseException {
        try {
            cl = new DefaultParser().parse(options, args, true);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            new HelpFormatter().printHelp("misim",
                "Configure your simulation with the following options.",
                options,
                "Please report any issues at "
                    + "https://github.com/Cambio-Project/resilience-simulator",
                true);
        }
        return cl;
    }

    /**
     * Tries to get the {@link CommandLine} object that represents the parsed command line arguments.
     *
     * @return the parsed command line arguments or {@code null} if they are not parsed yet.
     */
    public static CommandLine getCommandLine() {
        return cl;
    }
}
