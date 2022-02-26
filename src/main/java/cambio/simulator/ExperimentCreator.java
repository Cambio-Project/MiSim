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
import org.jetbrains.annotations.NotNull;

/**
 * Class that contains code for creating a new {@link desmoj.core.simulator.Experiment} based on a {@link
 * ExperimentStartupConfig}.
 *
 * @author Lion Wagner
 */
public final class ExperimentCreator {

    /**
     * Creates a new {@link Experiment} based on the given configuration.
     *
     * @param config startup configuration of the experiment
     * @return a new {@link Experiment} that is configured based on the given config
     */
    public static Experiment createSimulationExperiment(ExperimentStartupConfig config) {
        String archDescLocation = config.getArchitectureDescLoc();
        String expDescLocation;

        expDescLocation = config
            .getExperimentDescLoc() != null
            ? config.getExperimentDescLoc()
            : config.getScenario();

        File architectureDescription =
            tryGetDescription(archDescLocation, "architecture");

        File experimentDescription =
            tryGetDescription(expDescLocation, "experiment/scenario");

        MiSimModel model = new MiSimModel(architectureDescription, experimentDescription);
        return setupExperiment(config, model);
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
    private static Experiment setupExperiment(ExperimentStartupConfig config, MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        Path reportLocation = ExportUtils.prepareReportDirectory(config, model);
        Experiment exp = new Experiment(metaData.getExperimentName(), reportLocation.toString());
        model.connectToExperiment(exp);

        exp.setSeedGenerator(metaData.getSeed());
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(config.showProgressBarOn());
        exp.stop(new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));

        if (config.noTraces()) {
            exp.traceOff(new TimeInstant(0, metaData.getTimeUnit()));
        } else {
            exp.tracePeriod(new TimeInstant(0, metaData.getTimeUnit()),
                new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        }

        if (config.debugOutputOn()) {
            exp.debugPeriod(new TimeInstant(0, metaData.getTimeUnit()),
                new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
            exp.debugOn(new TimeInstant(0, metaData.getTimeUnit()));
        } else {
            exp.debugOff(new TimeInstant(0, metaData.getTimeUnit()));
        }

        return exp;
    }
}
