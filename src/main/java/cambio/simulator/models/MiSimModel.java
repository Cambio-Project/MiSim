package cambio.simulator.models;

import java.io.File;
import java.io.IOException;
import java.rmi.UnexpectedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.orchestration.MiSimSpecific.StatsMiSimTasksExecutor;
import cambio.simulator.orchestration.MicroserviceOrchestration;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.management.ScaleTaskExecutor;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.management.StatsTasksExecutor;
import cambio.simulator.orchestration.parsing.ConfigDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.YAMLParser;
import cambio.simulator.orchestration.scheduling.Scheduler;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import cambio.simulator.parsing.ModelLoader;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Main model that contains architectural and experiment descriptions/data.
 *
 * @author Lion Wagner
 */
public class MiSimModel extends Model {

    /**
     * general reporter, can be used if objects/classes do not want to create their own reporter or use a common
     * reporter.
     */
    public static MultiDataPointReporter generalReporter = new MultiDataPointReporter();

    public static boolean orchestrated;
    public static boolean createOrchestratedReport = true;

    private final transient File architectureModelLocation;
    private final transient File experimentModelOrScenarioLocation;
    //exp meta data
    private final transient ExperimentMetaData experimentMetaData;
    //arch model
    private transient ArchitectureModel architectureModel;
    //exp model
    private transient ExperimentModel experimentModel;

    /**
     * Creates a new MiSimModel and load the meta data from the experiment description.
     *
     * @param architectureModelLocation         Location of the architectural description.
     * @param experimentModelOrScenarioLocation Location of the experiment description.
     */
    public MiSimModel(File architectureModelLocation, File experimentModelOrScenarioLocation) {
        super(null, "MiSimModel", true, true);
        this.architectureModelLocation = architectureModelLocation;
        this.experimentModelOrScenarioLocation = experimentModelOrScenarioLocation;

        long startTime = System.nanoTime();
        this.experimentMetaData =
                ModelLoader.loadExperimentMetaData(experimentModelOrScenarioLocation, architectureModelLocation);
        this.experimentMetaData.markStartOfSetup(startTime);
    }


    @Override
    public String description() {
        return experimentMetaData.getModelName();
    }


    @Override
    public void init() {
        //check if orchestration mode should be active
        this.orchestrated = getExperimentMetaData().getOrchestrationDirectory() != null && getExperimentMetaData().isOrchestrated();
        this.architectureModel = ModelLoader.loadArchitectureModel(this);
        this.experimentModel = ModelLoader.loadExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
    }

    @Override
    public void doInitialSchedules() {
        this.experimentMetaData.markStartOfExperiment(System.nanoTime());

        if (orchestrated) {
            initOrchestration();
        } else {
            System.out.println("Using MiSim WITHOUT Container Orchestration");
            architectureModel.getMicroservices().forEach(Microservice::start);
            if (createOrchestratedReport) {
                final StatsMiSimTasksExecutor statsMiSimTasksExecutor = new StatsMiSimTasksExecutor(getModel(), "StatsMiSimExecutor", getModel().traceIsOn());
                statsMiSimTasksExecutor.doInitialSelfSchedule();
            }
        }
        List<ISelfScheduled> collect =  experimentModel.getAllSelfSchedulesEntities().stream().collect(Collectors.toList());
        collect.sort(new Comparator<ISelfScheduled>() {
            @Override
            public int compare(ISelfScheduled iSelfScheduled, ISelfScheduled t1) {
                return iSelfScheduled.getName().compareTo(t1.getName());
            }
        });

        for (ISelfScheduled selfScheduledEvent : collect) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        SimulationEndEvent simulationEndEvent =
                new SimulationEndEvent(this, SimulationEndEvent.class.getSimpleName(), true);
        simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
    }

    public void initOrchestration() {
        System.out.println();
        System.out.println("### Initializing Container Orchestration ###");
        String targetDir = getExperimentMetaData().getOrchestrationDirectory() + "/k8_files";
        ConfigDto configDto = null;
        try {
            configDto = YAMLParser.parseConfigFile(getExperimentMetaData().getOrchestrationDirectory() + "/environment/config.yaml");
        } catch (IOException | ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Cluster cluster = new Cluster(createNodesFromConfigDto(configDto));
        ManagementPlane.getInstance().setModel(this);
        ManagementPlane.getInstance().setCluster(cluster);

        try {
            assignPriosToSchedulers(configDto);
            assignStartUpTimesToInstances(configDto);
        } catch (UnexpectedException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final YAMLParser yamlParser = YAMLParser.getInstance();
        yamlParser.setArchitectureModel(architectureModel);
        yamlParser.setConfigDto(configDto);
        try {
            yamlParser.initDeploymentsFromArchitectureAndYAMLFiles(targetDir);
        } catch (ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final ScaleTaskExecutor scaleTaskExecutor = new ScaleTaskExecutor(getModel(), "MasterTaskExecutor", getModel().traceIsOn(), configDto.getScalingInterval());
        scaleTaskExecutor.doInitialSelfSchedule();
        if (MiSimModel.createOrchestratedReport) {
            System.out.println("[INFO]: Orchestration Report will be created afterwards\n");
            final StatsTasksExecutor statsTasksExecutor = new StatsTasksExecutor(getModel(), "StatsExecutor", getModel().traceIsOn());
            statsTasksExecutor.doInitialSelfSchedule();
        } else {
            System.out.println("[WARNING]: Orchestration Report will NOT be created afterwards\n");
        }

        System.out.println("### Initialization of Container Orchestration finished ###");
        System.out.println();

    }

    public List<Node> createNodesFromConfigDto(ConfigDto configDto) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < configDto.getNodes().getAmount(); i++) {
            nodes.add(new Node(getModel(), "Node" + i, traceIsOn(), configDto.getNodes().getCpu()));
        }

        if (configDto.getCustomNodes() != null) {
            for (ConfigDto.CustomNodes customNode : configDto.getCustomNodes()) {
                nodes.add(new Node(getModel(), customNode.getName(), traceIsOn(), customNode.getCpu()));
            }
        }


        return nodes;
    }

    public void assignPriosToSchedulers(ConfigDto configDto) throws UnexpectedException {
        if (configDto.getSchedulerPrio() != null) {
            for (ConfigDto.SchedulerPrio schedulerPrio : configDto.getSchedulerPrio()) {
                String name = schedulerPrio.getName();
                SchedulerType schedulerType1 = SchedulerType.fromString(name);
                Scheduler schedulerInstanceByType = Util.getInstance().getSchedulerInstanceByType(schedulerType1);
                schedulerInstanceByType.setPRIO(schedulerPrio.getPrio());
            }
        }
    }

    public void assignStartUpTimesToInstances(ConfigDto configDto) throws UnexpectedException {
        if (configDto.getStartUpTimeContainer() != null) {
            for (ConfigDto.StartUpTimeContainer startUpTimeContainer : configDto.getStartUpTimeContainer()) {
                String name = startUpTimeContainer.getName();
                Microservice service = architectureModel.getMicroservices().stream().filter(microservice -> microservice.getPlainName().equals(name)).findAny().orElse(null);
                if (service != null) {
                    ((MicroserviceOrchestration) service).setStartTime(startUpTimeContainer.getTime());
                }
            }
        }
    }

    public ArchitectureModel getArchitectureModel() {
        return architectureModel;
    }

    public ExperimentModel getExperimentModel() {
        return experimentModel;
    }

    public ExperimentMetaData getExperimentMetaData() {
        return experimentMetaData;
    }

    public File getArchitectureModelLocation() {
        return architectureModelLocation;
    }

    public File getExperimentModelOrScenarioLocation() {
        return experimentModelOrScenarioLocation;
    }
}
