package cambio.simulator.entities.networking;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.models.MiSimModel;

/**
 * Represents a {@code Request} between two {@code MicroserviceInstance}s. Hold information about the {@code
 * NetworkDependency} it tries to complete.
 *
 * @author Lion Wagner
 */
public class InternalRequest extends Request {

    private final ServiceDependencyInstance dependency;

    /**
     * Constructs a new Internal Request. Construction is based on the respective dependency and requester.
     *
     * @param dependency {@link ServiceDependencyInstance} that should be competed by this request.
     * @param requester  {@link MicroserviceInstance} that requests the answer to this request.
     */
    public InternalRequest(MiSimModel model, boolean showInTrace, ServiceDependencyInstance dependency,
                           MicroserviceInstance requester) {
        super(model,
            String
                .format("Cascading Request %s(%s)", dependency.getTargetOp().getOwnerMS().getPlainName(),
                    dependency.getTargetOp().getPlainName()),
            showInTrace,
            dependency.getParentRequest(),
            dependency.getTargetOp(), requester);
        dependency.updateChildRequest(this);
        this.dependency = dependency;
    }


    public ServiceDependencyInstance getDependency() {
        return dependency;
    }
}
