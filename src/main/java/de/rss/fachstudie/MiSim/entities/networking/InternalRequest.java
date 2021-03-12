package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class InternalRequest extends Request {

    private final NetworkDependency dependency;

    public InternalRequest(Model model, boolean showInTrace, NetworkDependency dependency, MicroserviceInstance requester) {
        super(model,
                String.format("Cascading Request %s(%s)", dependency.getTarget_op().getOwner(), dependency.getTarget_op()),
                showInTrace,
                dependency.getParent_request(),
                dependency.getTarget_op(), requester);
        dependency.updateChild_request(this);
        this.dependency = dependency;
    }


    public NetworkDependency getDependency() {
        return dependency;
    }
}
