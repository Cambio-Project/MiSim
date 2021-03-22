package de.rss.fachstudie.MiSim.entities.patterns;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.misc.Priority;
import de.rss.fachstudie.MiSim.parsing.FromJson;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * @author Lion Wagner
 */
public abstract class PeriodicServiceOwnedPattern extends ServiceOwnedPattern {

    @FromJson
    private double period = 1;
    @FromJson
    private double start = 0;
    @FromJson
    private double stop = Double.MAX_VALUE;

    private TimeSpan periodSpan;
    private PatternScheduler scheduler;

    public PeriodicServiceOwnedPattern(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace, owner);
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
            if (scheduler.isScheduled())
                scheduler.cancel();
            scheduler.passivate();
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }
    }
}
