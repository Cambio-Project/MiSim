package cambio.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;

import cambio.simulator.cli.CLI;
import cambio.simulator.export.ExportUtils;
import cambio.simulator.misc.FileUtilities;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.javatuples.Pair;

/**
 * Main class of the simulator. Takes care of triggering cli parsing, experiment creation and running.
 *
 * @author Lion Wagner
 */
public class Main {

    /**
     * Main entry point of the program.
     *
     * @param args program options, see {@link CLI}
     */
    public static void main(String[] args) {
        CommandLine cl = null;

        try {
            cl = CLI.parseArguments(args);
        } catch (ParseException e) {
            System.exit(1);
        }

        Pair<Experiment, MiSimModel> pair = createSimulationExperiment(cl);
        Experiment experiment = pair.getValue0();
        experiment.start();
        experiment.finish();
    }

    private static Pair<Experiment, MiSimModel> createSimulationExperiment(CommandLine cl) {
        String archDescLocation = cl.getOptionValue(CLI.archModelOpt.getOpt());
        String expDescLocation;

        if (cl.hasOption(CLI.expModelOpt.getOpt())) {
            expDescLocation = cl.getOptionValue(CLI.expModelOpt.getOpt());
        } else {
            expDescLocation = cl.getOptionValue(CLI.scenarioOpt.getOpt());
        }

        File architectureDescription = null;
        File experimentDescription = null;

        try {
            architectureDescription = FileUtilities.tryLoadExistingFile(archDescLocation);
        } catch (FileNotFoundException e) {
            System.out.printf("[Error] Could not find an architecture description at %s.%n", archDescLocation);
            System.exit(1);
        }
        try {
            experimentDescription = FileUtilities.tryLoadExistingFile(expDescLocation);
        } catch (FileNotFoundException e) {
            System.out.printf("[Error] Could not find an experiment/scenario description at %s.%n", archDescLocation);
            System.exit(1);
        }

        MiSimModel model = new MiSimModel(architectureDescription, experimentDescription);
        ExperimentMetaData metaData = model.getExperimentMetaData();

        Path reportLocation = ExportUtils.prepareReportFolder(model);
        metaData.setReportLocation(reportLocation);


        if (cl.hasOption(CLI.debugOutput.getOpt())) {
            model.debugOn();
        }

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

        return new Pair<>(exp, model);
    }

}