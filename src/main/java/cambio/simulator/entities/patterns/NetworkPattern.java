package cambio.simulator.entities.patterns;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import desmoj.core.simulator.Model;

/**
 * Represents a Pattern that is owned by a {@link MicroserviceInstance} and monitors with the network.
 * Patterns of this type listen automatically to every outgoing request of a {@link MicroserviceInstance}.
 *
 * @author Lion Wagner
 */
public abstract class NetworkPattern extends InstanceOwnedPattern implements IRequestUpdateListener {
    public NetworkPattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace, owner);
    }
}
