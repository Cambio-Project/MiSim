package cambio.simulator.entities.patterns;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * Event that represents the half-closing of a circuit breaker.
 *
 * @author Lion Wagner
 */
public class HalfOpenBreakerEvent extends NamedExternalEvent {

    private final ICircuitBreakerState stateToChange;

    /**
     * Constructs a new {@link HalfOpenBreakerEvent}.
     *
     * @param stateToChange circuit breaker that should change its state to half-open
     */
    public HalfOpenBreakerEvent(Model model, String name, boolean showInTrace,
                                ICircuitBreakerState stateToChange) {
        super(model, name, showInTrace);
        this.stateToChange = stateToChange;
        setSchedulingPriority(Priority.IMMEDIATELY_ON_TARGETED_TIME);
    }

    @Override
    public void onRoutineExecution() throws SuspendExecution {
        synchronized (NamedSimProcess.class) {
            stateToChange.toHalfOpen();
        }
    }
}
