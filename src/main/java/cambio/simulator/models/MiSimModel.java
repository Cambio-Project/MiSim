package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.orchestration.environment.Cluster;
import cambio.simulator.orchestration.ManagementPlane;
import cambio.simulator.orchestration.MasterTasksExecutor;
import cambio.simulator.orchestration.environment.Node;
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
    public static final boolean orchestrated = true;

    public static MultiDataPointReporter generalReporter = new MultiDataPointReporter();

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

        if(orchestrated){
            initOrchestration();
        } else {
            architectureModel.getMicroservices().forEach(Microservice::start);
        }

        final MasterTasksExecutor masterTasksExecutor = new MasterTasksExecutor(getModel(), "MasterTaskExecutor", getModel().traceIsOn());
        masterTasksExecutor.doInitialSelfSchedule();

        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        SimulationEndEvent simulationEndEvent =
            new SimulationEndEvent(this, SimulationEndEvent.class.getSimpleName(), true);
        simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
    }

    public void initOrchestration(){
        List<Node> nodes = new ArrayList<>();
        for(int i = 0; i<5; i++){
            nodes.add(new Node(getModel(), "Node", traceIsOn()));
        }
        Cluster cluster = new Cluster(nodes);
        final ManagementPlane managementPlane = ManagementPlane.getInstance();
        managementPlane.setModel(this);
        managementPlane.setCluster(cluster);
        managementPlane.populateSchedulerMap();
        managementPlane.buildDeploymentScheme(this.architectureModel);
        managementPlane.applyDeploymentScheme();
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
