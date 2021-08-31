package cambio.simulator.entities.patterns;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeSpan;

/**
 * @author Lion Wagner
 */
public class PeriodicPatternScheduler extends NamedSimProcess {
    private final IPeriodicServiceOwnedPattern owner;
    private final double start;
    private final double stop;
    private final TimeSpan periodSpan;

    public PeriodicPatternScheduler(Model model, IPeriodicServiceOwnedPattern owner, double start, double stop,
                                    double period) {
        super(model, null, true, false);
        this.owner = owner;
        this.start = start;
        this.stop = stop;
        periodSpan = new TimeSpan(period, getModel().getExperiment().getReferenceUnit());
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
