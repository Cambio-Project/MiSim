package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import cambio.simulator.parsing.FromJson;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * A service owned pattern that is automatically triggered periodically.
 *
 * @author Lion Wagner
 */
public abstract class PeriodicServiceOwnedPattern extends ServiceOwnedPattern {

    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double period = 1;
    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double start = 0;
    @FromJson
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double stop = Double.MAX_VALUE;

    private TimeSpan periodSpan;
    private PatternScheduler scheduler;

    public PeriodicServiceOwnedPattern(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace, owner);
    }

    @Override
    protected void onFieldInitCompleted() {
        periodSpan = new TimeSpan(period, getModel().getExperiment().getReferenceUnit());
        scheduler = new PatternScheduler(getModel(), this);
        scheduler.activate(new TimeInstant(start));
    }

    /**
     * Manually triggers this patterns' routine.
     */
    public void trigger() {
        onTriggered();
    }

    protected abstract void onTriggered();

    @Override
    public void shutdown() {
        try {
            if (scheduler.isScheduled()) {
                scheduler.cancel();
            }
            scheduler.passivate();
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }
    }

    private class PatternScheduler extends SimProcess {
        private final PeriodicServiceOwnedPattern owner;

        public PatternScheduler(Model model, PeriodicServiceOwnedPattern owner) {
            super(model, null, true, false);
            this.owner = owner;
            setSchedulingPriority(Priority.HIGH);
        }

        @Override
        public void lifeCycle() throws SuspendExecution {
            owner.onTriggered();
            if (presentTime().getTimeAsDouble(getModel().getExperiment().getReferenceUnit()) + start >= stop) {
                passivate();
                return;
            }
            this.hold(owner.periodSpan);
        }
    }
}
