package cambio.simulator.models;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    // TODO this should come from an external config (probably in experiment model "useOrchestration": true/false)
    public static final boolean orchestrated = true;

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
        this.architectureModel = ModelLoader.loadArchitectureModel(this);
        this.experimentModel = ModelLoader.loadExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
    }

    @Override
    public void doInitialSchedules() {
        this.experimentMetaData.markStartOfExperiment(System.nanoTime());

        if (orchestrated) {
            initOrchestration();
            final MasterTasksExecutor masterTasksExecutor = new MasterTasksExecutor(getModel(), "MasterTaskExecutor", getModel().traceIsOn());
            masterTasksExecutor.doInitialSelfSchedule();
        } else {
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
        // TODO this should come from an environment / cluster model
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            nodes.add(new Node(getModel(), "Node", traceIsOn()));
        }
        Cluster cluster = new Cluster(nodes);
        final ManagementPlane managementPlane = ManagementPlane.getInstance();
        managementPlane.setModel(this);
        managementPlane.setCluster(cluster);
        managementPlane.populateSchedulerMap();
        managementPlane.populateScalerMap();


        final YAMLParser yamlParser = YAMLParser.getInstance();
        yamlParser.setArchitectureModel(architectureModel);
        // TODO this should come from an external config (probably experiment model)
        String targetDir = "target/orchestration";
        final Set<String> fileNames = Util.getInstance().listFilesUsingJavaIO(targetDir);
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

        //Read other k8s obejcts that refer to deployments (e.g. HPA)
        for (String filePath : yamlParser.getRemainingFilePaths()) {
            try {
                yamlParser.applyManipulation(filePath);
            } catch (ParsingException | IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }


        //TODO check if whole architecture file matches to deployments
        /*
        Desired behavior:
        - if service is specified in k8s deployments and in architecture model -> one deployment created
        - if service is specified in k8s deployments but not in architecture model -> should result in a warining will
        not be created and not simulated
        - if service is not specified in k8s deployments but in the architecture model -> automatically create deployment,
        autoscaler, load balancer, scheduler etc. from default values or entry from architecture file
         */
        //TODO maybe read other K8s objects: service etc.


//        managementPlane.buildDeploymentScheme(this.architectureModel);
        System.out.println("Init Orchestration finished");
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
