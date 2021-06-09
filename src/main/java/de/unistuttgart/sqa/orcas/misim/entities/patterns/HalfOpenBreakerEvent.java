package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import co.paralleluniverse.fibers.SuspendExecution;
import de.unistuttgart.sqa.orcas.misim.misc.Priority;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * Event that represents the half-closing of a circuit breaker.
 *
 * @author Lion Wagner
 */
public class HalfOpenBreakerEvent extends ExternalEvent {

    private final CircuitBreakerState stateToChange;

    /**
     * Constructs a new {@link HalfOpenBreakerEvent}.
     *
     * @param stateToChange circuit breaker that should change its state to half-open
     */
    public HalfOpenBreakerEvent(Model model, String name, boolean showInTrace, CircuitBreakerState stateToChange) {
        super(model, name, showInTrace);
        this.stateToChange = stateToChange;
        setSchedulingPriority(Priority.IMMEDIATELY_ON_TARGETED_TIME);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        stateToChange.toHalfOpen();
    }
}
