package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.Microservice;

/**
 * @author Lion Wagner
 */
public interface IAutoscalingPolicy extends IStrategy {
    void apply(Microservice owner);
}
