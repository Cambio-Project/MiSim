package cambio.simulator.models;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.orchestration.Util;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.k8objects.K8Object;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.management.MasterTasksExecutor;
import cambio.simulator.orchestration.environment.Node;
import cambio.simulator.orchestration.parsing.ConfigDto;
import cambio.simulator.orchestration.parsing.ParsingException;
import cambio.simulator.orchestration.parsing.YAMLParser;
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
        this.orchestrated = getExperimentMetaData().getOrchestrationDirectory() != null;
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
        }

        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        SimulationEndEvent simulationEndEvent =
                new SimulationEndEvent(this, SimulationEndEvent.class.getSimpleName(), true);
        simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
    }

    public void initOrchestration() {
        String targetDir = getExperimentMetaData().getOrchestrationDirectory() + "/k8_files";
        ConfigDto configDto = null;
        try {
            configDto = YAMLParser.parseConfigFile(getExperimentMetaData().getOrchestrationDirectory() + "/environment/config.yaml");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Cluster cluster = new Cluster(createNodesFromConfigDto(configDto));
        //set hold times for scales
        final ManagementPlane managementPlane = ManagementPlane.getInstance();
        managementPlane.setModel(this);
        managementPlane.setCluster(cluster);
        managementPlane.populateSchedulerMap();


        final YAMLParser yamlParser = YAMLParser.getInstance();
        yamlParser.setArchitectureModel(architectureModel);
        final Set<String> fileNames;
        try {
            fileNames = Util.getInstance().listFilesUsingJavaIO(targetDir);
            // Read only deployments first
            for (String fileName : fileNames) {
                try {
                    String filePath = targetDir + "/" + fileName;
                    final K8Object k8Object = yamlParser.parseFile(filePath);
                    if (k8Object != null) {
                        if (k8Object instanceof Deployment) {
                            managementPlane.getDeployments().add((Deployment) k8Object);
                        } else {
                            throw new ParsingException("The parser returned an unknown K8Object");
                        }
                    }
                } catch (ParsingException | IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            //Read other k8s objects that refer to deployments (e.g. HPA)
            for (String filePath : yamlParser.getRemainingFilePaths()) {
                try {
                    yamlParser.applyManipulation(filePath);
                } catch (ParsingException | IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
        } catch (ParsingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        setHoldTimesForAllScalersFromConfigDto(configDto);

        //TODO check if whole architecture file matches to deployments
        /*
        Desired behavior:
        - if service is specified in k8s deployments and in architecture model -> one deployment created
        - if service is specified in k8s deployments but not in architecture model -> should result in a warning will
        not be created and not simulated
        - if service is not specified in k8s deployments but in the architecture model -> automatically create deployment,
        autoscaler, load balancer, scheduler etc. from default values or entry from architecture file
         */
        //TODO maybe read other K8s objects: service etc.


//        managementPlane.buildDeploymentScheme(this.architectureModel);

        final MasterTasksExecutor masterTasksExecutor = new MasterTasksExecutor(getModel(), "MasterTaskExecutor", getModel().traceIsOn());
        masterTasksExecutor.doInitialSelfSchedule();
        System.out.println("Init Orchestration finished");

    }

    public List<Node> createNodesFromConfigDto(ConfigDto configDto) {
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < configDto.getNodes().getAmount(); i++) {
            nodes.add(new Node(getModel(), "Node", traceIsOn(), configDto.getNodes().getCpu()));
        }
        return nodes;
    }

    public void setHoldTimesForAllScalersFromConfigDto(ConfigDto configDto) {
        ManagementPlane.getInstance().getDeployments().stream().filter(deployment -> deployment.getAutoScaler()!=null).collect(Collectors.toList()).stream().forEach(deployment -> {
            deployment.getAutoScaler().setHoldTimeUp(configDto.getScaler().getHoldTimeUpScaler());
            deployment.getAutoScaler().setHoldTimeDown(configDto.getScaler().getHoldTimeDownScaler());
    });
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
}
