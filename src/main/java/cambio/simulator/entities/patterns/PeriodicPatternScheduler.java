package cambio.simulator.entities.patterns;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.TimeSpan;

/**
 * Class that takes care of periodically triggering {@link IPeriodicPattern} objects.
 *
 * @author Lion Wagner
 * @see PeriodicServiceOwnedPattern
 * @see StrategicPeriodicServiceOwnedPattern
 */
public class PeriodicPatternScheduler extends NamedSimProcess {
    private final IPeriodicPattern owner;
    private final double start;
    private final double stop;
    private final TimeSpan periodSpan;

    /**
     * Creates a new scheduler.
     *
     * @param model    owning model
     * @param pattern  pattern that should be triggered by this scheduler.
     * @param start    simulation time at which the first trigger should happen.
     * @param stop     simulation time after which no more triggers should happen.
     * @param interval interval/period between triggers
     */
    public PeriodicPatternScheduler(MiSimModel model, IPeriodicPattern pattern, double start, double stop,
                                    double interval) {
        super(model, null, true, false);
        this.owner = pattern;
        this.start = start;
        this.stop = stop;
        periodSpan = new TimeSpan(interval, getModel().getExperiment().getReferenceUnit());
        setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        owner.onTriggered();
        if (presentTime().getTimeAsDouble(getModel().getExperiment().getReferenceUnit()) + start >= stop) {
            passivate();
            return;
        }
        this.hold(periodSpan);
    }
}
