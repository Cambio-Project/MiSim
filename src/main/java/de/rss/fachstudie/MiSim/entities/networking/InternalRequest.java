package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

/**
 * Represents a {@code Request} between two {@code MicroserviceInstance}s. Hold information about the {@code
 * NetworkDependency} it tries to complete.
 *
 * @author Lion Wagner
 */
public class InternalRequest extends Request {

    private final NetworkDependency dependency;

    /**
     * Constructs a new Internal Request. Construction is based on the respective dependency and requester.
     * @param dependency {@link NetworkDependency} that should be competed by this request.
     * @param requester {@link MicroserviceInstance} that requests the answer to this request.
     */
    public InternalRequest(Model model, boolean showInTrace, NetworkDependency dependency,
                           MicroserviceInstance requester) {
        super(model,
            String
                .format("Cascading Request %s(%s)", dependency.getTargetOp().getOwnerMS(), dependency.getTargetOp()),
            showInTrace,
            dependency.getParentRequest(),
            dependency.getTargetOp(), requester);
        dependency.updateChildRequest(this);
        this.dependency = dependency;
    }


    public NetworkDependency getDependency() {
        return dependency;
    }
}
