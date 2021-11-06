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
            CLI.parseArguments(args);
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
                "-" + CLI.archModelOpt.getOpt(), "somePath",
                "-" + CLI.expModelOpt.getOpt(), "someOtherPath"
            };
        try {
            CommandLine cli = CLI.parseArguments(args);
            Assertions.assertEquals(cli, CLI.getCommandLine());
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }

    @Test
    void requiresEitherScenarioOrExperiment() {
        String[] args = {};
        checkMissingRequiredArgument(args, CLI.expModelOpt.getOpt());

        args = new String[] {};
        checkMissingRequiredArgument(args, CLI.scenarioOpt.getOpt());

        args = new String[] {
            "-" + CLI.expModelOpt.getOpt(), "someOtherPath",
            "-" + CLI.scenarioOpt.getOpt(), "someOtherOtherPath"
        };

        String[] finalArgs = args;
        Assertions.assertThrows(AlreadySelectedException.class, () -> CLI.parseArguments(finalArgs));

    }

    @Test
    void requiresArchitectureFilePath() {
        String[] args = {};
        checkMissingRequiredArgument(args, CLI.archModelOpt.getOpt());
    }

    @Test
    void properInput() {
        String[] args =
            new String[] {
                "-" + CLI.archModelOpt.getOpt(), "somePath",
                "-" + CLI.expModelOpt.getOpt(), "someOtherPath"
            };
        try {
            CommandLine cli = CLI.parseArguments(args);
            Assertions.assertEquals("somePath", cli.getOptionValue(CLI.archModelOpt.getOpt()));
            Assertions.assertEquals("someOtherPath", cli.getOptionValue(CLI.expModelOpt.getOpt()));
        } catch (ParseException e) {
            e.printStackTrace();
            Assertions.fail();
        }
    }
}