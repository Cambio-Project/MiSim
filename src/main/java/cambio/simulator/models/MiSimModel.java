package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.nparsing.ModelLoader;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class MiSimModel extends Model {

    //exp meta data
    private final ExperimentMetaData experimentMetaData;
    //arch model
    private ArchitectureModel architectureModel;
    //exp model
    private ExperimentModel experimentModel;


    public MiSimModel(File architectureModelLocation, File experimentModelOrScenarioLocation) {
        super(null, "MiSimModel", true, true);
        long startTime = System.currentTimeMillis();
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
        this.experimentMetaData.markEndOfSetup(System.currentTimeMillis());
    }

    @Override
    public void doInitialSchedules() {
        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEvents()) {
            selfScheduledEvent.doInitialSelfSchedule();
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
}
