package de.rss.fachstudie.MiSim.models;

import de.rss.fachstudie.MiSim.entities.MessageObject;
import de.rss.fachstudie.MiSim.entities.generator.Generator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceScaleEvent;
import de.rss.fachstudie.MiSim.events.*;
import de.rss.fachstudie.MiSim.export.ExportReport;
import de.rss.fachstudie.MiSim.export.MultiDataPointReporter;
import de.rss.fachstudie.MiSim.export.ReportWriter;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.parsing.ArchModelParser;
import de.rss.fachstudie.MiSim.parsing.ExpModelParser;
import de.rss.fachstudie.MiSim.resources.CPU;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.statistic.TimeSeries;
import org.apache.commons.cli.*;
import org.apache.commons.math3.analysis.function.Exp;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Main class to start the experiment. This class will load the input file and create a model out of it.
 * doInitialSchedules Starts the inital event. initFields Gets called at the start of the experiment and loads all relevant
 * experiment resources.
 */
public class MainModel extends Model {
    private static final TimeUnit timeUnit = TimeUnit.SECONDS;
    private double simulationTime = 0;
    private String report = "";
    private int datapoints = -1;
    private double precision = 100000;
    private double statisticChunks = 10;
    private int seed = 0;
    private String resourcePath = "./Report/resources/";
    private boolean showInitEvent = true;
    private boolean showStartEvent = true;
    private boolean showStopEvent = true;
    private boolean showMonkeyEvent = true;

    // Queues
    public HashMap<Integer, Queue<Microservice>> services;
    public HashMap<Integer, Queue<MessageObject>> taskQueues;
    public static HashMap<Integer, Microservice> allMicroservices;

    public static Set<Microservice> microservices = new HashSet<>();

    // Resources
    public static HashMap<Integer, HashMap<Integer, CPU>> serviceCPU;

    // Statistics
    public HashMap<Integer, HashMap<Integer, TimeSeries>> activeThreadStatistics;
    public HashMap<Integer, HashMap<Integer, TimeSeries>> existingThreadStatistics;
    public HashMap<Integer, HashMap<Integer, TimeSeries>> cpuStatistics;
    public HashMap<Integer, HashMap<Integer, TimeSeries>> responseStatisitcs;
    public static HashMap<Integer, HashMap<Integer, TimeSeries>> circuitBreakerStatistics;
    public static HashMap<Integer, HashMap<Integer, TimeSeries>> threadPoolStatistics;
    public static HashMap<Integer, HashMap<Integer, TimeSeries>> threadQueueStatistics;
    public HashMap<Integer, HashMap<Integer, TimeSeries>> resourceLimiterStatistics;
    public HashMap<Integer, TimeSeries> taskQueueStatistics;

    public static MultiDataPointReporter generalReporter = new MultiDataPointReporter();

    public void setSimulationTime(double simTime) {
        if (simTime > 0)
            simulationTime = simTime;
        else
            simulationTime = 1000;
    }

    public static void main(String[] args) {
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
                System.out.println("No valid architecture file was given");
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
                System.out.println("No valid experiment file was given");
                System.exit(1);
                return;
            }
        }


        ExpModelParser.loadMetadata(Paths.get(exp_model));

//        if (validator.valideArchModel(archParser)) {
        long startTime = System.nanoTime();

        MainModel model = new MainModel(null, ExpModelParser.simulation_meta_data.get("model_name"), true, true);
        model.setSimulationTime(Double.parseDouble(ExpModelParser.simulation_meta_data.get("duration")));
        model.setChunkSize((int) (model.getSimulationTime() * 0.05));
        model.setReport(ExpModelParser.simulation_meta_data.get("report"));
        model.setDatapoints(Integer.parseInt(ExpModelParser.simulation_meta_data.get("datapoints")));
        model.setSeed(Integer.parseInt(ExpModelParser.simulation_meta_data.get("seed")));
        if (cmd.hasOption(debugOutput.getOpt())) model.debugOn();

        Experiment exp = new Experiment(ExpModelParser.simulation_meta_data.get("experiment_name"));
        model.connectToExperiment(exp);
        exp.setSeedGenerator(model.getSeed());
        exp.setShowProgressBarAutoclose(true);
        exp.setShowProgressBar(cmd.hasOption("p"));
        exp.stop(new TimeInstant(model.getSimulationTime(), getTimeUnit()));
        exp.tracePeriod(new TimeInstant(0, getTimeUnit()), new TimeInstant(model.getSimulationTime(), getTimeUnit()));
        exp.debugPeriod(new TimeInstant(0, getTimeUnit()), new TimeInstant(model.getSimulationTime(), getTimeUnit()));
        if (cmd.hasOption(debugOutput.getOpt())) exp.debugOn(new TimeInstant(0, getTimeUnit()));


//        ArchModelParser.parseArchModelFile(arch_model);
        Set<Microservice> microservices = ArchModelParser.parseArchModelFile(Paths.get(arch_model), model);
        MainModel.microservices = microservices;
        ExpModelParser.parseExperimentData(Paths.get(exp_model), model, microservices);


        long setupTime = System.nanoTime() - startTime;
        long tempTime = System.nanoTime();

        exp.start();

        long experimentTime = System.nanoTime() - tempTime;
        tempTime = System.nanoTime();

        //exp.report();
        exp.finish();

        try {
            Files.createDirectories(Paths.get(".", "Report", "raw"));
            Files.write(Paths.get(".", "Report", "raw", "_meta.txt"), ("{\"duration\": " + ExpModelParser.simulation_meta_data.get("duration") + "}").getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, TreeMap<TimeInstant, Object>> data = de.rss.fachstudie.MiSim.export.ReportCollector.getInstance().collect_data();
        TreeMap<String, TreeMap<TimeInstant, Object>> sortedData = new TreeMap<>(data);
        ReportWriter.writeReporterCollectorOutput(sortedData);

//        if (!ExpModelParser.simulation_meta_data.get("report").equals("none")) {
//            ExportReport exportReport = new ExportReport(model);
//            System.out.println("\nCreated Report successfully.");
//        }

        long reportTime = System.nanoTime() - tempTime;
        long executionTime = System.nanoTime() - startTime;

        System.out.println("\n*** Simulator ***");
        System.out.println("Simulation of Architecture: " + arch_model);
        System.out.println("Executed Experiment:        " + ExpModelParser.simulation_meta_data.get("experiment_name"));
        System.out.println("Setup took:                 " + model.timeFormat(setupTime));
        System.out.println("Experiment took:            " + model.timeFormat(experimentTime));
        System.out.println("Report took:                " + model.timeFormat(reportTime));
        System.out.println("Execution took:             " + model.timeFormat(executionTime));
        Toolkit.getDefaultToolkit().beep();
//        } else {
//            System.out.println("Your inserted input was not valide. Please check correctness of you JSON file.");
//        }
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public void setDatapoints(int points) {
        if (points > 0)
            this.datapoints = points;
        if (points > simulationTime || points == -1) {
            points = (int) simulationTime;
        }
    }

    public int getDatapoints() {
        return this.datapoints;
    }

    public double getPrecision() {
        return precision;
    }

    public double getStatisticChunks() {
        return this.statisticChunks;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public static TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public boolean getShowInitEvent() {
        return showInitEvent;
    }

    public boolean getShowStartEvent() {
        return showStartEvent;
    }

    public boolean getShowStopEvent() {
        return showStopEvent;
    }

    public boolean getShowMonkeyEvent() {
        return showMonkeyEvent;
    }

    /**
     * Helper Function to get the id of a microservice instance by the name.
     *
     * @param name the name of the service to get the name from
     * @return id of the corresponding microservice if successful, otherwise -1
     */
    public int getIdByName(String name) {

        for (int i = 0; i < allMicroservices.size(); i++) {
            if (name.equals(allMicroservices.get(i).getName())) {
                return allMicroservices.get(i).getId();
            }
        }
        return -1;
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
        return "This model is a test of Desmoj to investigate the suitability of Desmoj for the simulation of microservice architectures.";
    }

    /**
     * Initialize static model components like distributions and queues.
     */
    @Override
    public void init() {


    }

    public static Microservice getMicroserviceFromName(String name) {
        return MainModel.microservices.stream()
                .filter(microservice -> microservice.getName().matches("^" + name + "(#[0-9]*)?$"))
                .findFirst()
                .orElse(null);
    }

    private String timeFormat(long nanosecs) {
        long tempSec = nanosecs / (1000 * 1000 * 1000);
        long ms = (nanosecs / (1000 * 1000)) % 1000;
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;

        if (day > 0)
            return String.format("%dd %dh %dm %ds %dms", day, hour, min, sec, ms);
        else if (hour > 0)
            return String.format("%dh %dm %ds %dms", hour, min, sec, ms);
        else if (min > 0)
            return String.format("%dm %ds %dms", min, sec, ms);
        else if (sec > 0)
            return String.format("%ds %dms", sec, ms);
        return String.format("%dms", ms);
    }

    public void log(String message) {
        System.out.println(this.presentTime() + ": \t" + message);
    }

    public void setChunkSize(int chunks) {
        statisticChunks = chunks;
    }

    /**
     * Place all events on the internal event list of the simulator which are necessary to start the simulation.
     */
    @Override
    public void doInitialSchedules() {

        MainModel.microservices.forEach(Microservice::start); //initalizes spawning of instances

        for (Generator generator : ExpModelParser.generators) {
            generator.eventRoutine();
        }

        for (ChaosMonkeyEvent chaosmonkey : ExpModelParser.chaosmonkeys) {
            chaosmonkey.schedule(chaosmonkey.getTargetTime());
        }

        for (LatencyMonkeyEvent latencymonkey : ExpModelParser.latencymonkeys) {
            latencymonkey.schedule(latencymonkey.getTargetTime());
        }

        for (SummonerMonkeyEvent summoner : ExpModelParser.summoners) {
            summoner.schedule(summoner.getTargetTime());
        }

//        MicroserviceScaleEvent scaleEvent = new MicroserviceScaleEvent(this, "ScaleEvent", true, getMicroserviceFromName("loon-service"), 2);
//        scaleEvent.schedule(new TimeInstant(120));

        //Fire off the finish event which is called during at the end of the simulation
        FinishEvent event = new FinishEvent(this, "Finishing Event", false);
        event.setSchedulingPriority(Priority.HIGH);
        event.schedule(new TimeInstant(simulationTime));
    }
}
