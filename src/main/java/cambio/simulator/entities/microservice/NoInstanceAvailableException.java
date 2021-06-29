package cambio.simulator.entities.microservice;

import cambio.simulator.entities.patterns.LoadBalancer;

/**
 * RuntimeException that is thrown if there are currently no requests service instances available.
 *
 * @author Lion Wagner
 * @see Microservice#getNextAvailableInstance()
 * @see LoadBalancer#getNextInstance()
 */
public class NoInstanceAvailableException extends RuntimeException {
}
