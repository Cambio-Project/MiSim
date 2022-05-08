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
@JsonTypeName(value = "round-robin-strict", alternativeNames = {"round_robin_strict", "roundrobin-strict"})
public class RoundRobinLoadbalancer implements ILoadBalancingStrategy {

    Deque<MicroserviceInstance> queue = new ArrayDeque<>();
    HashSet<MicroserviceInstance> queued = new HashSet<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances)
        throws NoInstanceAvailableException {

        //add all newly running instances to the front of the queue
        runningInstances.forEach(instance -> {
            if (!queued.contains(instance)) {
                queue.addFirst(instance);
                queued.add(instance);
            }
        });

        //find the next running instance
        MicroserviceInstance nextTargetInstance = null;
        do {
            queued.remove(nextTargetInstance); // remove 'nextTargetInstance', as it is not running
            if ((nextTargetInstance = queue.poll()) == null) { //null -> queue is empty -> no instance can be found
                throw new NoInstanceAvailableException();
            }
        } while (nextTargetInstance.getState() != InstanceState.RUNNING); //discard not running instances

        queue.addLast(nextTargetInstance); //add the instance to the end of the queue

        assert queue.size() == queued.size();

        return nextTargetInstance;
    }
}
