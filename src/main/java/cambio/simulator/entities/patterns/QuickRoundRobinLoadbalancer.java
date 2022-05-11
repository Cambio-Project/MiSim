package cambio.simulator.entities.patterns;

import java.util.*;

import cambio.simulator.entities.microservice.*;
import cambio.simulator.parsing.JsonTypeName;

/**
 * Implements a simple round-robin load balancing strategy. This strategy is fair, but not a very strict round-robin. It
 * will only consider new instances once all already running instances have received a request. This allows this
 * implementation to be very fast, but it does not assign requests to newly created instances immediately. This should
 * only become a problem if the demand of single request is extremely high and can be worked around by maually modifying
 * the order of instances in the input collection, as the round-robin keeps the input order.
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "round-robin_fast", alternativeNames = {
    "round_robin_quick",
    "round_robin_fast",
    "roundrobin_fast"})
public class QuickRoundRobinLoadbalancer implements ILoadBalancingStrategy {

    Queue<MicroserviceInstance> queue = new ArrayDeque<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances)
        throws NoInstanceAvailableException {
        // if the queue is empty, fill it with the available instances
        if (queue.isEmpty()) {
            queue.addAll(runningInstances);
        }

        //find the next running instance
        MicroserviceInstance nextTargetInstance;
        do {
            if ((nextTargetInstance = queue.poll()) == null) { //queue is empty -> no instance can be found
                throw new NoInstanceAvailableException();
            }
        } while (nextTargetInstance.getState() != InstanceState.RUNNING); //discard not running instances

        return nextTargetInstance;
    }
}
