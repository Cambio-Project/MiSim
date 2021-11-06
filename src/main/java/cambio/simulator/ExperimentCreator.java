package cambio.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import cambio.simulator.export.ExportUtils;
import cambio.simulator.misc.FileUtilities;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public final class ExperimentCreator {
    public static Experiment createSimulationExperiment(CommandLine cl) {
        String archDescLocation = cl.getOptionValue(CLI.archModelOpt.getOpt());
        String expDescLocation;

        expDescLocation = cl.getOptionValue(
            cl.hasOption(CLI.expModelOpt.getOpt())
                ? CLI.expModelOpt.getOpt()
                : CLI.scenarioOpt.getOpt());

        File architectureDescription =
            tryGetDescription(archDescLocation, "architecture");

        File experimentDescription =
            tryGetDescription(expDescLocation, "experiment/scenario");

        MiSimModel model = new MiSimModel(architectureDescription, experimentDescription);

        if (cl.hasOption(CLI.debugOutput.getOpt())) {
            model.debugOn();
        }

        return setupExperiment(cl, model);
    }


    @NotNull
    private static File tryGetDescription(String expDescLocation, String descriptionName) {
        File experimentDescription = null;
        try {
            experimentDescription = FileUtilities.tryLoadExistingFile(expDescLocation);
        } catch (FileNotFoundException e) {
            System.out.printf("[Error] Could not find an %s description at %s.%n", descriptionName, expDescLocation);
            System.exit(2);
        }
        return experimentDescription;
    }


    @NotNull
    private static Experiment setupExperiment(CommandLine cl, MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        Path reportLocation = ExportUtils.prepareReportDirectory(model);
        Experiment exp = new Experiment(metaData.getExperimentName(), reportLocation.toString());
        model.connectToExperiment(exp);

        exp.setSeedGenerator(metaData.getSeed());
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(cl.hasOption(CLI.showProgressBar.getOpt()));
        exp.stop(new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        exp.tracePeriod(new TimeInstant(0, metaData.getTimeUnit()),
            new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        if (cl.hasOption(CLI.debugOutput.getOpt())) {
            exp.debugPeriod(new TimeInstant(0, metaData.getTimeUnit()),
                new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
            exp.debugOn(new TimeInstant(0, metaData.getTimeUnit()));
        }

        return exp;
    }
}
