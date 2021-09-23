package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.Microservice;

/**
 * Interface for auto scaling policies.
 *
 * @author Lion Wagner
 */
public interface IAutoscalingPolicy extends IStrategy {
    void apply(Microservice owner);
}
