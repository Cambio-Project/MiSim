package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import desmoj.core.simulator.Model;

/**
 * Represents a Pattern that is owned by a {@link MicroserviceInstance} and monitors with the network.
 * Patterns of this type listen automatically to every outgoing request of a {@link MicroserviceInstance}.
 *
 * <p>
 * For usage see {@link InstanceOwnedPattern}.
 *
 * @author Lion Wagner
 * @see IPatternLifeCycleHooks
 */
public abstract class InstanceOwnedNetworkPattern extends InstanceOwnedPattern implements IRequestUpdateListener {
    public InstanceOwnedNetworkPattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace, owner);
    }
}
