package cambio.simulator.models;

import java.io.File;
import java.time.LocalDateTime;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.parsing.ModelLoader;
import desmoj.core.simulator.*;

/**
 * Main model that contains architectural and experiment descriptions/data.
 *
 * @author Lion Wagner
 */
public class MiSimModel extends Model {

    //exp meta data
    private final ExperimentMetaData experimentMetaData;
    //arch model
    private ArchitectureModel architectureModel;
    //exp model
    private ExperimentModel experimentModel;

    /**
     * Creates a new MiSimModel and loads the metadata from the experiment model.
     *
     * <p>
     * Use {@link #connectToExperiment(Experiment)} to initialize the model.
     *
     * @param architectureModelLocation         Location of the architectural description.
     * @param experimentModelOrScenarioLocation Location of the experiment description.
     */
    public MiSimModel(File architectureModelLocation, File experimentModelOrScenarioLocation) {
        super(null, "MiSimModel", false, false);
        long startTime = System.nanoTime();
        this.experimentMetaData =
            ModelLoader.loadExperimentMetaData(experimentModelOrScenarioLocation, architectureModelLocation);
        this.experimentMetaData.markStartOfSetup(startTime);
    }


    @Override
    public String description() {
        //TODO: replace this with actual description instead of simply its name
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

        architectureModel.getMicroservices().forEach(Microservice::start);

        for (ISelfScheduled selfScheduledEvent : experimentModel.getAllSelfSchedulesEntities()) {
            selfScheduledEvent.doInitialSelfSchedule();
        }
        SimulationEndEvent simulationEndEvent =
            new SimulationEndEvent(this, SimulationEndEvent.class.getSimpleName(), true);
        simulationEndEvent.schedule(new TimeInstant(this.experimentMetaData.getDuration()));
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
