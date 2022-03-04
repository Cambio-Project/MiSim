package cambio.simulator.entities.patterns;

import desmoj.core.simulator.TimeInstant;
import org.javatuples.Tuple;

/**
 * Interface for classes that represent the State of a Circuit Breaker.
 */
public interface ICircuitBreakerState {
    Tuple getCurrentStatistics();

    CircuitBreakerState getState();

    boolean isOpen();

    /**
     * Notify the circuit breaker state that a request was sent successfully.
     */
    void notifySuccessfulCompletion(TimeInstant when);

    /**
     * Notify the circuit breaker state that a request failed.
     */
    void notifyArrivalFailure(TimeInstant when);

    /**
     * Method called by the {@link HalfOpenBreakerEvent} to half open this circuit after a certain amount of time.
     */
    void toHalfOpen();
}
