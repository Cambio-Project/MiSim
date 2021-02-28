package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class NetworkRequestReceiveEvent extends Event<Request> {
    public NetworkRequestReceiveEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(final Request request) throws SuspendExecution {
        Request requestInstance = request;
        if (requestInstance instanceof RequestAnswer) {
            requestInstance = ((RequestAnswer) requestInstance).unpack();
        }

        if (requestInstance.isCompleted()) {
            requestInstance.stampReceived(presentTime());
            if (requestInstance.hasParent()) { //cascading request, notify parent its done
                Request parent_request = requestInstance.getParent();
                parent_request.notifyDependencyHasFinished(requestInstance);
            } else {
                throw new IllegalStateException("Internal Error: Receive Event caught a request without parent (no receiver).\n" + requestInstance.toString());
            }
        } else {
            Microservice receivingMicroservice = requestInstance.operation.getOwner();

            try {
                MicroserviceInstance instance = receivingMicroservice.getNextAvailableInstance();
                requestInstance.setHandler(instance);
                instance.handle(requestInstance);
            } catch (NoInstanceAvailableException e) {
                new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), e).schedule(requestInstance);

            }

        }
    }
}
