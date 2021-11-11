package cambio.simulator;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.AlreadySelectedException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CLITest {

    private static void checkMissingRequiredArgument(String[] args, String expectedMissingArg) {
        try {
            CLI.parseArguments(ExperimentStartupConfig.class, args);
        } catch (MissingOptionException e) {
            for (Object missingOption : e.getMissingOptions()) {

                if (missingOption instanceof String
                    && expectedMissingArg.equals(missingOption)) {
                    return;
                }

                List<Option> optionsToCheck = new LinkedList<>();
                if (missingOption instanceof OptionGroup) {
                    optionsToCheck.addAll(((OptionGroup) missingOption).getOptions());
                }
                for (Option option : optionsToCheck) {
                    if (option.getOpt().equals(expectedMissingArg)
                        || option.getLongOpt().equals(expectedMissingArg)) {
                        return;
                    }
                }

            }
            Assertions.fail(
                String.format("Expected missing argument '%s' was not detected missing.", expectedMissingArg));
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail("Expected MissingArgumentException");
        }
    }

    @Test
    void setGlobalCLI() {
        String[] args =
            new String[] {
                "-a", "somePath",
                "-e", "someOtherPath"
            };
        try {
            CLI.parseArgumentsToCommandLine(ExperimentStartupConfig.class, args);
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void requiresEitherScenarioOrExperiment() {
        String[] args = {};
        checkMissingRequiredArgument(args, "e");

        args = new String[] {};
        checkMissingRequiredArgument(args, "s");

        args = new String[] {
            "-e", "someOtherPath",
            "-s", "someOtherOtherPath"
        };

        String[] finalArgs = args;
        Assertions.assertThrows(AlreadySelectedException.class,
            () -> CLI.parseArguments(ExperimentStartupConfig.class, finalArgs));

    }

    @Test
    void requiresArchitectureFilePath() {
        String[] args = {};
        checkMissingRequiredArgument(args, "a");
    }

    @Test
    void properInput() {
        String[] args =
            new String[] {
                "-a", "somePath",
                "-e", "someOtherPath"
            };
        try {
            CommandLine cli = CLI.parseArgumentsToCommandLine(ExperimentStartupConfig.class, args);
            Assertions.assertEquals("somePath", cli.getOptionValue("a"));
            Assertions.assertEquals("someOtherPath", cli.getOptionValue("e"));
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }
}