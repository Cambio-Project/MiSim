package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.parsing.ModelLoader;
import desmoj.core.simulator.Model;

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

        long startTime = System.currentTimeMillis();
        this.experimentMetaData =
            ModelLoader.loadExperimentMetaData(experimentModelOrScenarioLocation, architectureModelLocation);
        experimentMetaData.setDurationOfMetaDataLoading(System.currentTimeMillis() - startTime);
    }


    @Override
    public String description() {
        return experimentMetaData.getModelName();
    }


    @Override
    public void init() {
        this.experimentMetaData.markStartOfSetup(System.currentTimeMillis());
        this.architectureModel = ModelLoader.loadArchitectureModel(this);
        this.experimentModel = ModelLoader.loadExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
        this.experimentMetaData.markEndOfSetup(System.currentTimeMillis());
    }

    @Override
    public void doInitialSchedules() {
        architectureModel.getMicroservices().forEach(Microservice::start);

        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        new SimulationEndEvent(this, "FinisherEvent", true);
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
