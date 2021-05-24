package de.rss.fachstudie.MiSim.models;

import com.google.gson.Gson;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.events.FinishEvent;
import de.rss.fachstudie.MiSim.events.ISelfScheduled;
import de.rss.fachstudie.MiSim.export.ExportReport;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.export.ReportCollector;
import de.rss.fachstudie.MiSim.export.ReportWriter;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.misc.Util;
import de.rss.fachstudie.MiSim.parsing.GsonParser;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.TreeMap;

/**
 * Main class to start the experiment. This class will load the input file and create a model out of it.
 * doInitialSchedules Starts the inital event. initFields Gets called at the start of the experiment and loads all
 * relevant experiment resources.
 */
public class MainModel extends Model {

    /**
     * general reporter, can be used if objects/classes do not want to create their own reporter or use a common reporter.
     */
    public static MultiDataPointReporter generalReporter = new MultiDataPointReporter();
    private static MainModel instance = null;

    public static MainModel get() {
        if (instance == null) {
            throw new IllegalStateException("MainModel was not initialized yet.");
        }
        return instance;
    }

    public static MainModel initialize(String modelName) {
        if (instance != null) {
            throw new IllegalStateException("Architecture Model was already initialized.");
        }
        instance = new MainModel(null, modelName, true, true);
        return get();
    }


    public static void main(String[] args) {
        //Option parsing
        String arch_model;
        String exp_model;

        Options options = new Options();

        Option arch_model_opt = new Option("a", "arch_model", true, "arch_model file path");
        arch_model_opt.setRequired(true);
        options.addOption(arch_model_opt);

        Option exp_model_opt = new Option("e", "exp_model", true, "exp_model file path");
        exp_model_opt.setRequired(true);
        options.addOption(exp_model_opt);

        Option progressbar = new Option("p", "progress_bar", false, "show progress bar during simulation");
        progressbar.setRequired(false);
        options.addOption(progressbar);

        Option debugOutput = new Option("d", "debug", false, "activate debugoutput");
        debugOutput.setRequired(false);
        options.addOption(debugOutput);


        CommandLineParser cmdparser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = cmdparser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("Simulator", options);
            System.exit(1);
            return;
        }

        arch_model = cmd.getOptionValue(arch_model_opt.getOpt());
        exp_model = cmd.getOptionValue(exp_model_opt.getOpt());

        if (arch_model.equals("")) {
            System.out.println("No architecture was specified");
            System.exit(1);
            return;
        } else {
            File f = new File(arch_model);
            if (!f.exists() || f.isDirectory()) {
                System.out.printf("Did not find architecture file at %s%n", f.getAbsolutePath());
                System.exit(1);
                return;
            }
        }

        if (exp_model.equals("")) {
            System.out.println("No experiment was specified");
            System.exit(1);
            return;
        } else {
            File f = new File(exp_model);
            if (!f.exists() || f.isDirectory()) {
                System.out.printf("Did not find experiment file at %s%n", f.getAbsolutePath());
                System.exit(1);
                return;
            }
        }


        //data/modeling parsing | setup
        long startTime = System.nanoTime();

        ExperimentMetaData metaData = ExperimentMetaData.initialize(Paths.get(exp_model), Paths.get(arch_model));

        MainModel model = MainModel.initialize(metaData.getModelName());
        if (cmd.hasOption(debugOutput.getOpt())) model.debugOn();

        Experiment exp = new Experiment(metaData.getExperimentName());
        model.connectToExperiment(exp);
        exp.setSeedGenerator(metaData.getSeed());
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(cmd.hasOption("p"));
        exp.stop(new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        exp.tracePeriod(new TimeInstant(0, metaData.getTimeUnit()), new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        exp.debugPeriod(new TimeInstant(0, metaData.getTimeUnit()), new TimeInstant(metaData.getDuration(), metaData.getTimeUnit()));
        if (cmd.hasOption(debugOutput.getOpt()))
            exp.debugOn(new TimeInstant(0, metaData.getTimeUnit()));


        ArchitectureModel.initialize(Paths.get(arch_model));
        ExperimentModel.initialize(Paths.get(exp_model));

        long setupTime = System.nanoTime() - startTime;
        long tempTime = System.nanoTime();

        //run experiment
        exp.start();

        long experimentTime = System.nanoTime() - tempTime;
        tempTime = System.nanoTime();

        //exp.report();
        exp.finish();

        //create report if wanted
        if (!metaData.getReportType().equals("none")) {
            generate_report(model);
        }

        long reportTime = System.nanoTime() - tempTime;
        long executionTime = System.nanoTime() - startTime;

        System.out.println("\n*** Simulator ***");
        System.out.println("Simulation of Architecture: " + arch_model);
        System.out.println("Executed Experiment:        " + metaData.getExperimentName());
        System.out.println("Setup took:                 " + Util.timeFormat(setupTime));
        System.out.println("Experiment took:            " + Util.timeFormat(experimentTime));
        System.out.println("Report took:                " + Util.timeFormat(reportTime));
        System.out.println("Execution took:             " + Util.timeFormat(executionTime));
    }

    private static void generate_report(MainModel model) {
        ExperimentMetaData metaData = ExperimentMetaData.get();
        Path reportLocation = Paths.get(".", "Report_" + metaData.getExperimentName());
        Gson gson = new GsonParser().getGson();
        try {
            FileUtils.deleteDirectory(reportLocation.toFile());
            reportLocation.toFile().mkdirs();
            String json = gson.toJson(metaData);
            Files.write(Paths.get(String.valueOf(reportLocation), "meta.json"), json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            Files.copy(metaData.getArchFileLocation().toPath(), Paths.get(String.valueOf(reportLocation), "arch.json"));
            Files.copy(metaData.getExpFileLocation().toPath(), Paths.get(String.valueOf(reportLocation), "exp.json"));

            final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/*.py");

            Files.walkFileTree(Paths.get("./Report"), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                    if (pathMatcher.matches(path)) {
                        Files.copy(path,Paths.get(String.valueOf(reportLocation),path.toFile().getName()));
                    }
                    return FileVisitResult.CONTINUE;
                }

//                @Override
//                public FileVisitResult visitFileFailed(Path file, IOException exc)
//                        throws IOException {
//                    return FileVisitResult.CONTINUE;
//                }
            });

            //export legacy graph
            new ExportReport(model);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, TreeMap<Double, Object>> data = ReportCollector.getInstance().collect_data();
        TreeMap<String, TreeMap<Double, Object>> sortedData = new TreeMap<>(data);
        ReportWriter.writeReporterCollectorOutput(sortedData, reportLocation);

    }

    public MainModel(Model owner, String modelName, boolean showInReport, boolean showInTrace) {
        super(owner, modelName, showInReport, showInTrace);
    }

    /**
     * Required method which returns a description for the model.
     *
     * @return the description of the model
     */
    @Override
    public String description() {
        return "This is the central model of the simulation of microservice architectures.";
    }

    /**
     * Initialize static model components like distributions and queues.
     */
    @Override
    public void init() {


    }


    public void log(String message) {
        System.out.println(this.presentTime() + ": \t" + message);
    }

    /**
     * Place all events on the internal event list of the simulator which are necessary to start the simulation.
     */
    @Override
    public void doInitialSchedules() {

        ArchitectureModel.get().getMicroservices().forEach(Microservice::start); //initalizes spawning of instances

        for (ISelfScheduled event : ExperimentModel.get().getAllSelfSchedulesEvents()) {
            event.doInitialSelfSchedule();
        }

        //Fire off the finish event which is called during at the end of the simulation
        FinishEvent event = new FinishEvent(this, "Finishing Event", false);
        event.setSchedulingPriority(Priority.HIGH);
        event.schedule(new TimeInstant(ExperimentMetaData.get().getDuration()));
    }
}
