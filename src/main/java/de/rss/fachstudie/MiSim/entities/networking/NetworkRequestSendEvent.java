package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.microservice.NoInstanceAvailableException;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Event that represents the sending of a request.
 * <p>
 * Can introduce network delay.
 * <p>
 * May be canceled during (and before) the travelling time of the request.
 *
 * @author Lion Wagner
 */
public class NetworkRequestSendEvent extends NetworkRequestEvent {

    private static final AtomicLong counterSendEvents = new AtomicLong(0);

    public static long getCounterSendEvents() {
        return counterSendEvents.get();
    }

    public static void resetCounterSendEvents() {
        counterSendEvents.set(0);
    }

    private static NumericalDist<Double> rng;

    private NetworkRequestReceiveEvent receiverEvent;
    private final Microservice targetService;
    private final MicroserviceInstance targetInstance;
    private boolean isCanceled = false;

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request, MicroserviceInstance target) {
        this(model, name, showInTrace, request, null, target);
    }

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request, Microservice target) {
        this(model, name, showInTrace, request, target, null);
    }

    private NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request, Microservice targetService, MicroserviceInstance targetInstance) {
        super(model, name, showInTrace, request);
        this.targetService = targetService;
        this.targetInstance = targetInstance;
        request.setSendEvent(this);

        if(rng == null) //a dirty fix to avoid memory leakage
            rng = new ContDistNormal(getModel(),  "DefaultNetworkDelay_RNG", 20, 10, true, false);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        traveling_request.stampSendoff(presentTime());

        counterSendEvents.getAndIncrement();

        if (traveling_request instanceof RequestAnswer && traveling_request.getParent() instanceof UserRequest) {
            // if an answer to a UserRequest is send, it will be considered done (since there is no receiver)
            traveling_request.stampReceived(presentTime());
            traveling_request.getParent().stampReceived(presentTime());
            updateListener.onRequestArrivalAtTarget(traveling_request, presentTime());
            updateListener.onRequestResultArrivedAtRequester(traveling_request.getParent(), presentTime());
            return;
        }



        //calculate next delay
        double nextDelay;
        do {
            nextDelay = rng.sample() / 1000;
        } while (nextDelay < 0); //ensures a positive delay, due to "infinite" gaussian deviation

        nextDelay = customizeLatency(nextDelay);

        //Apply custom latency and/or add delay of latency injection
        updateListener.onRequestSend(traveling_request, presentTime());
        if (isCanceled) return; //this event might get canceled by the sending listeners

        MicroserviceInstance targetInstance = retrieveTargetInstance();
        if (targetInstance == null) {
            NetworkRequestEvent cancelEvent = new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), traveling_request,
                    RequestFailedReason.NO_INSTANCE_AVAILABLE,
                    String.format("No Instance for Service %s was available.", targetService.getQuotedName()));
            cancelEvent.schedule(new TimeSpan(nextDelay));
        } else {
            receiverEvent = new NetworkRequestReceiveEvent(getModel(), String.format("Receiving of %s", traveling_request.getQuotedName()), traceIsOn(), traveling_request, targetInstance);
            receiverEvent.schedule(new TimeSpan(nextDelay));

            //TODO: schedule NetworkRequestTimeOutCheckEvent, to check the request after a timeout duration

            traveling_request.setReceiveEvent(receiverEvent);
        }
    }

    private double customizeLatency(double nextDelay) {
        if (this.traveling_request instanceof UserRequest) return 0;

        double modifiedDelay = nextDelay;
        if (traveling_request.hasParent()) {
            NetworkDependency dep = traveling_request.getParent().getRelatedDependency(traveling_request);
            if (traveling_request instanceof RequestAnswer) {
                Request parent = ((RequestAnswer) traveling_request).unpack();
                dep = parent.getParent().getRelatedDependency(parent);
            }

            if (dep.hasCustomDelay()) {
                modifiedDelay = dep.getNextCustomDelay();
            }
            modifiedDelay += dep.getNextExtraDelay();
        }
        return modifiedDelay;
    }


    public void cancel() {
        super.cancel();

        setCanceled();

        // An answer to a UserRequest cannot be canceled, since they are not send back to the user.
        // Rather they are considered completed once the answer is send by the handling instance.
        if (traveling_request instanceof RequestAnswer && traveling_request.getParent() instanceof UserRequest) return;

        if (receiverEvent != null && receiverEvent.isScheduled())
            receiverEvent.cancel();
        new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), traveling_request, RequestFailedReason.REQUESTING_INSTANCE_DIED, "Sending was forcibly aborted!");
    }

    protected MicroserviceInstance retrieveTargetInstance() {
        if (targetInstance != null) return targetInstance;
        else if (targetService == null) throw new IllegalStateException("Sender cant find a valid target instance.");
        else {
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
