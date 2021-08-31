package cambio.simulator.entities.microservice;

import java.util.Collection;

import cambio.simulator.entities.patterns.ILoadBalancingStrategy;

/**
 * RuntimeException that is thrown if there are currently no requests service instances available.
 *
 * @author Lion Wagner
 * @see Microservice#getNextAvailableInstance()
 * @see ILoadBalancingStrategy#getNextInstance(Collection) 
 */
public class NoInstanceAvailableException extends RuntimeException {
}
