package cambio.simulator.entities.networking;

import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * Event that should be scheduled when a request gets canceled.
 *
 * @author Lion Wagner
 */
public class NetworkRequestCanceledEvent extends NetworkRequestEvent {

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
    }
}
