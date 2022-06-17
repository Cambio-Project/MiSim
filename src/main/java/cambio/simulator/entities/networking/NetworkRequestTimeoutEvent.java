package cambio.simulator.entities.networking;

import java.util.concurrent.TimeUnit;

import java.util.HashMap;
import java.util.Map;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;

/**
 * Event that represents the timeout of a {@link Request}. Is automatically scheduled with each send and canceled
 * automatically on failure or receive.
 *
 * <p>
 * This event is automatically skipped if the {@link Request} reaches its destination successfully or is canceled
 * otherwise.
 * </p>
 */
public class NetworkRequestTimeoutEvent extends NetworkRequestEvent implements IRequestUpdateListener {

    private static final Map<Microservice, Integer> microserviceTimeoutMap = new HashMap<>();

    public static Map<Microservice, Integer> getMicroserviceTimeoutMap() {
        return microserviceTimeoutMap;
    }

    private boolean canceled = false;

    /**
     * Constructs and schedules a timeout of a request.
     *
     * @param request the request that should be able to time out.
     */
    public NetworkRequestTimeoutEvent(Model model, String name, boolean showInTrace, Request request) {
        super(model, name, showInTrace, request);
        this.setSchedulingPriority(Priority.LOW);
        this.schedule(new TimeSpan(8, TimeUnit.SECONDS));
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        if (canceled) {
            return;
        }

        Microservice owner = getTravelingRequest().getHandler().getOwner();
        if (owner != null) {
            microserviceTimeoutMap.merge(owner, 1, Integer::sum);
        }

        NetworkRequestEvent cancelEvent =
            new NetworkRequestCanceledEvent(getModel(), "RequestCancel", getModel().traceIsOn(), travelingRequest,
                RequestFailedReason.TIMEOUT,
                "Request " + travelingRequest.getName() + " was canceled due to a timeout.");
        cancelEvent.schedule(new TimeSpan(0));
    }

    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        canceled = true;
        return false;
    }

    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        canceled = true;
        return false;
    }

    @Override
    public int getListeningPriority() {
        return Priority.NORMAL + 1;
    }
}
