package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * Event that represents the sending of a request.
 * <p>
 * Can introduce network delay.
 *
 * @author Lion Wagner
 */
public class NetworkRequestSendEvent extends NetworkRequestEvent {

    private NetworkRequestReceiveEvent receiverEvent;
    private final NumericalDist<Double> rng;

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, Request request, IRequestUpdateListener listener) {
        super(model, name, showInTrace, listener, request);
        rng = new ContDistNormal(model, name + "_RNG", 20, 10, true, false);
        request.setSendEvent(this);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        traveling_request.stampSendoff(presentTime());
        Request request = traveling_request;

        if (request instanceof RequestAnswer && request.getParent() instanceof UserRequest) {
            request = ((RequestAnswer) request).unpack(); //unpack the request if its an answer to a UserRequest
        }

        if (!request.hasParent()) { // if it is completed and has no parent the request ist considered done
            request.stampReceived(presentTime());
            return;
        }

        //calculate next delay
        double nextDelay;
        do {
            nextDelay = rng.sample() / 1000;
        } while (nextDelay < 0); //ensures a positive delay, due to "infinite" gaussian deviation


        //TODO: add network delay from DelayMonkey

        receiverEvent = new NetworkRequestReceiveEvent(getModel(), String.format("Receiving of %s", request.getQuotedName()), traceIsOn(), updateListener, request);
        receiverEvent.schedule(new TimeSpan(nextDelay));

        //TODO: schedule NetworkRequestTimeOutCheckEvent, to check the request after a timeout duration
        //TODO: depending on implementation one could also check for instance availability here to simulate client side load balancing behavior more accurately

        traveling_request.setReceiveEvent(receiverEvent);
        updateListener.onRequestSend(traveling_request);
    }


    public void cancel() {
        super.cancel();
        receiverEvent.cancel();
        new NetworkRequestCanceledEvent(getModel(), "RequestCanceledEvent", traceIsOn(), updateListener, traveling_request, "Sending was forcibly aborted!");

    }

}
