package cambio.simulator.events;

import java.util.Objects;

import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.TimeInstant;

/**
 * Represents an event that can do an initial self schedule if asked to.
 *
 * @author Lion Wagner
 */
public abstract class SelfScheduledExperimentAction extends ExperimentAction implements ISelfScheduled {

    public SelfScheduledExperimentAction(MiSimModel owner, String name, boolean showInTrace) {
        super(owner, name, showInTrace);
    }

    @Override
    public void doInitialSelfSchedule() {
        this.schedule(new TimeInstant(initialArrivalTime));
    }

    public void setTargetTime(TimeInstant targetTime) {
        Objects.requireNonNull(targetTime);
        this.initialArrivalTime = targetTime.getTimeAsDouble();
    }

}
