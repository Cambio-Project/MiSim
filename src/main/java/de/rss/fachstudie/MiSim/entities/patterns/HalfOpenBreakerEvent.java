package de.rss.fachstudie.MiSim.entities.patterns;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.misc.Priority;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class HalfOpenBreakerEvent extends ExternalEvent {

    private final CircuitBreakerState stateToChange;


    public HalfOpenBreakerEvent(Model model, String s, boolean b, CircuitBreakerState stateToChange) {
        super(model, s, b);
        this.stateToChange = stateToChange;
        setSchedulingPriority(Priority.IMMEDIATELY_ON_TARGETED_TIME);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        stateToChange.toHalfOpen();
    }
}
