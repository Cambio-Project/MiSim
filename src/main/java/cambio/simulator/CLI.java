package cambio.simulator;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import cambio.simulator.misc.Util;
import com.google.gson.internal.UnsafeAllocator;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Static class that holds the command line options of the simulator and is able to parse these into a {@link
 * CommandLine} object for retrieval.
 *
 * @author Lion Wagner
 */
public final class CLI {

    /**
     * Parses the given arguments into the given dataclass using {@link CLIOption}s and a {@link CommandLine} under the
     * hood.
     *
     * <p>
     * This method unsafely creates a  new instance of the given dataclass. So no initialization logic (constructors,
     * setters and similar) will be called.
     *
     * <p>
     * Use {@link CLIOption} to mark (and configure) witch fields of your dataclass should be parsed.
     *
     * @param dataclass class that should contain the parsed values of the arguments
     * @param args      program arguments
     * @param <T>       type of the dataclass
     * @return a new object of the dataclass
     * @throws ParseException if the command line arguments cannot be parsed properly to the given options
     */
    public static <T> @NotNull T parseArguments(Class<T> dataclass, String[] args) throws ParseException {
        return CLI.parseCommandLineToDataObject(CLI.parseArgumentsToCommandLine(dataclass, args), dataclass);
    }

    /**
     * Parses the given arguments into a {@link CommandLine} object using the optionsProviderClass. Specifically it
     * scans the optionsProviderClass for {@link CLIOption}s and creates a {@link Options} based on this.
     *
     * @param optionsProviderClass class that provides with options
     * @param args                 program arguments
     * @return a new {@link CommandLine} object with the parsed arguments
     * @throws ParseException if the command line arguments cannot be parsed properly to the given options
     * @see CLIOption
     */
    public static @NotNull CommandLine parseArgumentsToCommandLine(Class<?> optionsProviderClass, final String[] args)
        throws ParseException {
        Options options = new Options();
        Field[] fields = Util.getAllFields(optionsProviderClass);

        Map<String, OptionGroup> optionGroups = new HashMap<>();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(CLIOption.class)) {
                continue;
            }

            CLIOption cliOption = field.getAnnotation(CLIOption.class);
            if (cliOption.opt().equals("") && cliOption.longOpt().equals("")) {
                throw new IllegalArgumentException("CLIOption annotation must have either opt or longOpt set!");
            }

            Option.Builder optionBuilder = Option.builder();
            if (!cliOption.opt().equals("")) {
                optionBuilder = Option.builder(cliOption.opt());
            }

            if (!cliOption.longOpt().equals("")) {
                optionBuilder.longOpt(cliOption.longOpt());
            }

            optionBuilder.desc(cliOption.description())
                .hasArg(cliOption.hasArg())
                .required(cliOption.required());

            final Option option = optionBuilder.build();
            options.addOption(option);

            if (!StringUtils.isBlank(cliOption.optionGroup())) {
                OptionGroup tmpGroup = new OptionGroup();
                tmpGroup.setRequired(cliOption.optionGroupRequired());
                tmpGroup.addOption(option);

                optionGroups.merge(cliOption.optionGroup(), tmpGroup,
                    (currentGroup, newGroup) -> {
                        if (!currentGroup.isRequired() && newGroup.isRequired()) {
                            currentGroup.getOptions().forEach(newGroup::addOption);
                            return newGroup;
                        }
                        currentGroup.addOption(option);
                        return currentGroup;
                    });
            }
        }

        optionGroups.values().forEach(options::addOptionGroup);


        CommandLine cl;
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
            throw e;
        }
        return cl;
    }

    /**
     * Creates a new {@link ExperimentStartupConfig} based on the given {@link CommandLine} that should be created via
     * the {@link CLI} class.
     *
     * @param cl {@link CommandLine} that contains the parsed cli options.
     * @return a new parsed {@link ExperimentStartupConfig}
     */
    public static <T> @NotNull T parseCommandLineToDataObject(CommandLine cl, Class<? extends T> baseClass) {
        T targetObject;
        try {
            targetObject = UnsafeAllocator.create().newInstance(baseClass);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassCastException("Could not create instance of " + baseClass.getName());
        }


        for (Field targetField : Util.getAllFields(baseClass)) {
            if (!targetField.isAnnotationPresent(CLIOption.class)) {
                continue;
            }
            targetField.setAccessible(true);
            Class<?> targetType = targetField.getType();

            CLIOption option = targetField.getAnnotation(CLIOption.class);

            try {
                String optName = !option.opt().equals("") ? option.opt() : option.longOpt();
                Object value = cl.getParsedOptionValue(optName);

                if (value == null && targetType == Boolean.TYPE) {
                    targetField.set(targetObject, cl.hasOption(optName));
                } else if (String.class.isAssignableFrom(targetType)) {
                    targetField.set(targetObject, value);
                } else if (String[].class.isAssignableFrom(targetType)) {
                    targetField.set(targetObject, value);
                } else {
                    throw new ClassCastException("Can only parse CLI options to the types of boolean/Boolean, String "
                        + "or String[].");
                }
            } catch (ParseException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return targetObject;
    }

}
