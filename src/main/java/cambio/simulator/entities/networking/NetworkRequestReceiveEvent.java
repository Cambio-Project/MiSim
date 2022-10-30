package cambio.simulator.entities.networking;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * Event that represents the successful arrival of a request at its target instance.
 *
 * <p>
 * Gives the traveling request to the receiving handler on arrival.
 *
 * @author Lion Wagner
 */
public class NetworkRequestReceiveEvent extends NetworkRequestEvent {

    private final MicroserviceInstance receivingInstance;

    public NetworkRequestReceiveEvent(Model model, String name, boolean showInTrace,
                                      Request travelingRequest,
                                      MicroserviceInstance receiver) {
        super(model, name, showInTrace, travelingRequest);
        receivingInstance = receiver;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        travelingRequest.stampReceivedAtHandler(presentTime());

        try {
            receivingInstance.handle(travelingRequest);

            if (travelingRequest instanceof RequestAnswer) {
                updateListener
                    .onRequestResultArrivedAtRequester(((RequestAnswer) travelingRequest).unpack(), presentTime());
            }

            updateListener.onRequestArrivalAtTarget(travelingRequest, presentTime());
        } catch (IllegalStateException e) {
            NetworkRequestEvent event = new NetworkRequestCanceledEvent(getModel(),
                "CANCEL Event for " + travelingRequest.getQuotedName(), traceIsOn(), travelingRequest,
                RequestFailedReason.HANDLING_INSTANCE_DIED);
            event.schedule(presentTime());
        }

    }
}
