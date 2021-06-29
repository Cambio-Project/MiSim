package cambio.simulator.entities.networking;

import java.util.concurrent.atomic.AtomicLong;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * Event that represents the sending of a request. Can introduce network delay. May be canceled during (and before) the
 * travelling of the request.
 *
 * @author Lion Wagner
 */
public class NetworkRequestSendEvent extends NetworkRequestEvent {

    private static final AtomicLong counterSendEvents = new AtomicLong(0);
    private static NumericalDist<Double> rng;
    private final Microservice targetService;
    private final MicroserviceInstance targetInstance;
    private NetworkRequestReceiveEvent receiverEvent;
    private NetworkRequestTimeoutEvent timeoutEvent;
    private boolean isCanceled = false;

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request,
                                   MicroserviceInstance target) {
        this(model, name, showInTrace, request, null, target);
    }

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request,
                                   Microservice target) {
        this(model, name, showInTrace, request, target, null);
    }

    private NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request,
                                    Microservice targetService, MicroserviceInstance targetInstance) {
        super(model, name, showInTrace, request);
        this.targetService = targetService;
        this.targetInstance = targetInstance;
        request.setSendEvent(this);

        //TODO: remove dirty fix to avoid memory leakage
        if (rng == null) {
            rng = new ContDistNormal(getModel(), "DefaultNetworkDelay_RNG", 1.6, 0.6, true, false);
        }
    }

    public static long getCounterSendEvents() {
        return counterSendEvents.get();
    }

    public static void resetCounterSendEvents() {
        counterSendEvents.set(0);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        travelingRequest.stampSendoff(presentTime());

        counterSendEvents.getAndIncrement();

        if (travelingRequest instanceof RequestAnswer && travelingRequest.getParent() instanceof UserRequest) {
            // if an answer to a UserRequest is send, it will be considered done (since there is no receiver)
            travelingRequest.stampReceived(presentTime());
            travelingRequest.getParent().stampReceived(presentTime());
            updateListener.onRequestArrivalAtTarget(travelingRequest, presentTime());
            updateListener.onRequestResultArrivedAtRequester(travelingRequest.getParent(), presentTime());
            return;
        }


        //calculate next delay
        double nextDelay;
        do {
            nextDelay = rng.sample() / 1000;
        } while (nextDelay < 0); //ensures a positive delay, due to "infinite" gaussian deviation

        nextDelay = customizeLatency(nextDelay);

        //Apply custom latency and/or add delay of latency injection
        updateListener.onRequestSend(travelingRequest, presentTime());
        if (isCanceled) {
            return; //this event might get canceled by the sending listeners
        }

        MicroserviceInstance targetInstance = retrieveTargetInstance();
        if (targetInstance == null) {
            NetworkRequestEvent cancelEvent =
                new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), travelingRequest,
                    RequestFailedReason.NO_INSTANCE_AVAILABLE,
                    String.format("No Instance for Service %s was available.", targetService.getQuotedName()));
            cancelEvent.schedule(new TimeSpan(nextDelay));
        } else {
            receiverEvent = new NetworkRequestReceiveEvent(getModel(),
                String.format("Receiving of %s", travelingRequest.getQuotedName()), traceIsOn(), travelingRequest,
                targetInstance);
            receiverEvent.schedule(new TimeSpan(nextDelay));

            timeoutEvent =
                new NetworkRequestTimeoutEvent(getModel(), "Timeout Checker for " + travelingRequest.getName(),
                    getModel().traceIsOn(), travelingRequest);
            travelingRequest.addUpdateListener(timeoutEvent);

            travelingRequest.setReceiveEvent(receiverEvent);
        }
    }

    private double customizeLatency(double nextDelay) {
        if (this.travelingRequest instanceof UserRequest) {
            return 0;
        }

        double modifiedDelay = nextDelay;
        if (travelingRequest.hasParent()) {
            NetworkDependency dep = travelingRequest.getParent().getRelatedDependency(travelingRequest);
            if (travelingRequest instanceof RequestAnswer) {
                Request parent = ((RequestAnswer) travelingRequest).unpack();
                dep = parent.getParent().getRelatedDependency(parent);
            }

            if (dep == null) {
                return modifiedDelay;
            }

            if (dep.hasCustomDelay()) {
                modifiedDelay = dep.getNextCustomDelay();
            }
            modifiedDelay += dep.getNextExtraDelay();
        }
        return modifiedDelay;
    }

    /**
     * Cancels the send event. Also cancels the relative {@link NetworkRequestReceiveEvent} and {@link
     * NetworkRequestTimeoutEvent} events. Triggers a {@link NetworkRequestCanceledEvent}.
     */
    public void cancel() {
        super.cancel();

        setCanceled();

        // An answer to a UserRequest cannot be canceled, since they are not send back to the user.
        // Rather they are considered completed once the answer is send by the handling instance.
        if (travelingRequest instanceof RequestAnswer && travelingRequest.getParent() instanceof UserRequest) {
            return;
        }

        if (receiverEvent != null && receiverEvent.isScheduled()) {
            receiverEvent.cancel();
        }
        if (timeoutEvent != null && timeoutEvent.isScheduled()) {
            timeoutEvent.cancel();
        }
        new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), travelingRequest,
            RequestFailedReason.REQUESTING_INSTANCE_DIED, "Sending was forcibly aborted!");
    }

    protected MicroserviceInstance retrieveTargetInstance() {
        if (targetInstance != null) {
            return targetInstance;
        } else if (targetService == null) {
            throw new IllegalStateException("Sender cant find a valid target instance.");
        } else {
            try {
                return targetService.getNextAvailableInstance();
            } catch (NoInstanceAvailableException e) {
                return null;
                //TODO: maybe do a special case if the whole service is killed?
            }
        }
    }

    public void setCanceled() {
        this.isCanceled = true;
    }
}
