package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
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

    private final MicroserviceInstance receivingInstance;

    public NetworkRequestReceiveEvent(Model model, String name, boolean showInTrace, Request request, MicroserviceInstance receiver) {
        super(model, name, showInTrace, request);
        receivingInstance = receiver;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        traveling_request.stampReceivedAtHandler(presentTime());

        try {
            receivingInstance.handle(traveling_request);

            if (traveling_request instanceof RequestAnswer) {
                updateListener.onRequestResultArrivedAtRequester(((RequestAnswer) traveling_request).unpack(), presentTime());
            }

            updateListener.onRequestArrivalAtTarget(traveling_request, presentTime());
        } catch (IllegalStateException e) {
            NetworkRequestEvent event = new NetworkRequestCanceledEvent(getModel(), String.format("CANCEL Event for %s", traveling_request.getQuotedName()), traceIsOn(), traveling_request, RequestFailedReason.HANDLING_INSTANCE_DIED);
            event.schedule(presentTime());
        }

    }
}
