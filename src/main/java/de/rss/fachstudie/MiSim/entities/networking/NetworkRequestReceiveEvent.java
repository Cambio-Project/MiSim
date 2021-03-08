package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

/**
 * Event that represents the successful arrival of a request at its target instance.
 * <p>
 * Gives the traveling request to the receiving handler on arrival.
 *
 * @author Lion Wagner
 */
public class NetworkRequestReceiveEvent extends NetworkRequestEvent {


    public NetworkRequestReceiveEvent(Model model, String name, boolean showInTrace, Request request) {
        super(model, name, showInTrace, request);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        Request requestInstance = traveling_request;

        if (requestInstance instanceof RequestAnswer) {
            requestInstance = ((RequestAnswer) requestInstance).unpack(); //unpack if its a request answer
        }

        if (requestInstance.isCompleted()) {
            requestInstance.stampReceived(presentTime());//if the request is completed stamp it as (results) received

            if (requestInstance.hasParent()) { //if there is a parent, the request is a cascading request, therefore: notify the parent request that its dependency answer has arrived
                Request parent_request = requestInstance.getParent();
                parent_request.notifyDependencyHasFinished(requestInstance);
                updateListener.onRequestArrivalAtTarget(traveling_request);
            } else {
                throw new IllegalStateException("Internal Error: Receive Event caught a request without parent (don't know where to send this).\n" + requestInstance.toString());
            }
        } else {

            Microservice receivingMicroservice = requestInstance.operation.getOwner();
            try {
                //let the owning microservice instance decide which instance should handle this Request
                //TODO: this can be moved to NetworkRequestSendEvent
                MicroserviceInstance instance = receivingMicroservice.getNextAvailableInstance();
                requestInstance.setHandler(instance);
                instance.handle(requestInstance); //give request to handler
                updateListener.onRequestArrivalAtTarget(traveling_request);
            } catch (NoInstanceAvailableException e) { //if no instance is available we tell the listener that its canceled (indirectly via the CancelEvents)
                new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), traveling_request,
                        String.format("No Instance for Service %s was available.", receivingMicroservice.getQuotedName()))
                        .schedule(presentTime());
            }
            //TODO: maybe do a special case if the whole service is killed?
        }
    }
}
