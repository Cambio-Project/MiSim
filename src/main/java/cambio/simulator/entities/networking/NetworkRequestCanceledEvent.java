package cambio.simulator.entities.networking;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

import java.util.HashMap;
import java.util.Map;

/**
 * Event that should be scheduled when a request gets canceled.
 *
 * @author Lion Wagner
 */
public class NetworkRequestCanceledEvent extends NetworkRequestEvent {
    public static Map<Microservice, Integer> microserviceCanceledMap = new HashMap<>();
    public static int counter = 0;
    private final RequestFailedReason reason;
    private final String details;

    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Request request,
                                       RequestFailedReason reason) {
        this(model, name, showInTrace, request, reason, null);
    }

    /**
     * Creates an event that notifies {@link IRequestUpdateListener}s of the failing of a request.
     *
     * @param request request
     * @param reason  why the request canceled/failed
     * @param details optional reasoning string that is used in the trace
     */
    public NetworkRequestCanceledEvent(Model model, String name, boolean showInTrace, Request request,
                                       RequestFailedReason reason, String details) {
        super(model, name, showInTrace, request);
        this.reason = reason;
        this.details = details;
        setSchedulingPriority(Priority.VERY_HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        sendTraceNote(
            String.format("Request %s was not handled. Cause: %s", travelingRequest.getQuotedName(), reason));
        if (details != null) {
            sendTraceNote(String.format("Details: %s", details));
        }

        updateListener.onRequestFailed(travelingRequest, presentTime(), reason);
        counter++;

        Microservice owner = null;
        if(getTravelingRequest().getHandler()!=null){
            getTravelingRequest().getHandler().getOwner();
        }else{
            owner = getTravelingRequest().getParent().getHandler().getOwner();
        }

        if (owner != null) {
            if (microserviceCanceledMap.get(owner) != null) {
                microserviceCanceledMap.put(owner, microserviceCanceledMap.get(owner) + 1);
            } else {
                microserviceCanceledMap.put(owner, 1);
            }
        }
    }
}
