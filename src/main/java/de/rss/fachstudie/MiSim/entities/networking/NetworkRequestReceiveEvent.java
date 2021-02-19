package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.Microservice;
import de.rss.fachstudie.MiSim.entities.MicroserviceInstance;
import de.rss.fachstudie.MiSim.models.MainModel;

/**
 * @author Lion Wagner
 */
public class NetworkRequestReceiveEvent extends MainModelAwareRequestEvent {
    public NetworkRequestReceiveEvent(MainModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        if (request.isCompleted()) {
            request.stampReceived(presentTime());
            if (request.hasParent()) { //cascading request, notify parent its done
                Request parent_request = request.getParent();
                parent_request.notifyDependencyHasFinished(request);
            } else {
                throw new IllegalStateException("Internal Error: Receive Event caught a request without parent (no receiver).\n" + request.toString());
            }
        } else {
            Microservice receivingMicroservice = request.operation.getOwner();

            try {
                MicroserviceInstance instance = receivingMicroservice.getNextAvailableInstance();
                request.setHandler(instance);
                instance.handle(request);
            } catch (NoInstanceAvailableException e) {
                new NetworkRequestCancledEvent(model, "RequestCanceledEvent", traceIsOn(), e).schedule(request);

            }

        }
    }
}
