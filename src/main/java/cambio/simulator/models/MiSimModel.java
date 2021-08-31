package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.FinishEvent;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.MultiDataPointReporter;
import cambio.simulator.parsing.ModelLoader;
import desmoj.core.simulator.Model;

/**
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
    private ExperimentMetaData experimentMetaData;
    //arch model
    private ArchitectureModel architectureModel;
    //exp model
    private ExperimentModel experimentModel;

    public MiSimModel(File architectureModelLocation, File experimentModelOrScenarioLocation) {
        super(null, "MiSimModel", true, true);
        this.architectureModelLocation = architectureModelLocation;
        this.experimentModelOrScenarioLocation = experimentModelOrScenarioLocation;
    }


    @Override
    public String description() {
        return experimentMetaData.getModelName();
    }


    @Override
    public void init() {
        long startTime = System.currentTimeMillis();
        this.experimentMetaData =
            ModelLoader.loadExperimentMetaData(experimentModelOrScenarioLocation, architectureModelLocation);
        this.experimentMetaData.markStartOfSetup(startTime);
        this.architectureModel = ModelLoader.loadArchitectureModel(this);
        this.experimentModel = ModelLoader.loadExperimentModel(this);
        this.experimentMetaData.setStartDate(LocalDateTime.now());
        this.experimentMetaData.markEndOfSetup(System.currentTimeMillis());
    }

    @Override
    public void doInitialSchedules() {
        architectureModel.getMicroservices().forEach(Microservice::start);
        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEvents()) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        new FinishEvent(this, "FinisherEvent", true);
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
