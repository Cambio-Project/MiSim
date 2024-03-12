package cambio.simulator;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;

import cambio.simulator.behavior.EventBusConnector;
import cambio.simulator.behavior.MTLActivationListener;
import cambio.simulator.export.ExportUtils;
import cambio.simulator.misc.FileUtilities;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.tltea.interpreter.BehaviorInterpretationResult;
import cambio.tltea.interpreter.Interpreter;
import cambio.tltea.interpreter.connector.Brokers;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * Class that contains code for creating a new {@link desmoj.core.simulator.Experiment} based on a
 * {@link ExperimentStartupConfig}.
 *
 * @author Lion Wagner
 */
public class ExperimentCreator {

    /**
     * Creates a new {@link Experiment} based on the given configuration.
     *
     * @param config startup configuration of the experiment
     * @return a new {@link Experiment} that is configured based on the given config
     */
    public Experiment createSimulationExperiment(ExperimentStartupConfig config) {
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
    protected File tryGetDescription(String expDescLocation, String descriptionName) {
        File experimentDescription = null;
        try {
            experimentDescription = FileUtilities.tryLoadExistingFile(expDescLocation);
        } catch (FileNotFoundException e) {
            System.out.printf("[Error] Could not find an %s description at %s.%n", descriptionName, expDescLocation);
            System.exit(2);
        }
        return experimentDescription;
    }

    /**
     * Parsing additional configuration options, besides the model locations.
     */
    @NotNull
    private static Experiment setupExperiment(ExperimentStartupConfig config, @NotNull MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        metaData.setStartDate(LocalDateTime.now());
        Path reportLocation = ExportUtils.prepareReportDirectory(config, model);
        Experiment exp = config.traceEnabled()
            ? new Experiment(metaData.getExperimentName(), reportLocation.toString())
            : new Experiment(metaData.getExperimentName(), reportLocation.toString(), "desmoj.core.report.NullOutput",
            "desmoj.core.report.NullOutput", "desmoj.core.report.NullOutput", "desmoj.core.report.NullOutput");
        model.connectToExperiment(exp);

        exp.setSeedGenerator(metaData.getSeed());
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(config.showProgressBarOn());
        exp.stop(new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));

        if (config.traceEnabled()) {
            exp.tracePeriod(new TimeInstant(0, metaData.getTimeUnit()),
                new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        } else {
            exp.traceOff(new TimeInstant(0, metaData.getTimeUnit()));
        }

        if (config.debugOutputOn()) {
            exp.debugPeriod(new TimeInstant(0, metaData.getTimeUnit()),
                new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
            exp.debugOn(new TimeInstant(0, metaData.getTimeUnit()));
        } else {
            exp.debugOff(new TimeInstant(0, metaData.getTimeUnit()));
        }

        if (config.mtlLoc() != null) {
            parseMtlFormula(config.mtlLoc(), model, config.debugOutputOn());
        }
        return exp;
    }

    private static void parseMtlFormula(String mtlLoc, @NotNull MiSimModel model, boolean isDebugOn) {
        for (BehaviorInterpretationResult result : Interpreter.INSTANCE.interpretAllAsBehavior(mtlLoc, new Brokers(),
            isDebugOn)) {
            new MTLActivationListener(result, model);
            result.getTriggerManager()
                .getEventActivationListeners()
                .forEach(listener -> EventBusConnector.createActivators(listener, model));
            result.activateProcessing();
        }
    }
}