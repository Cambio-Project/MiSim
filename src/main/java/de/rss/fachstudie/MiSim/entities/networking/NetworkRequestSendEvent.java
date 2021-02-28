package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public class NetworkRequestSendEvent extends Event<Request> {

    private final MicroserviceInstance sender;
    private NetworkRequestReceiveEvent receiverEvent;
    private final NumericalDist<Double> rng;

    public NetworkRequestSendEvent(Model model, String name, boolean showInTrace, MicroserviceInstance sender) {
        super(model, name, showInTrace);
        this.sender = sender;
        rng = new ContDistNormal(model, name + "_RNG", 20, 10, true, false);
    }


    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        request.stampSendoff(presentTime());

        if(request instanceof RequestAnswer && request.getParent() instanceof UserRequest){
            request = ((RequestAnswer) request).unpack(); //unpack the request if its an answer to a UserRequest
        }

        if (!request.hasParent()) { // if it is completed and has no parent we are done
            request.stampReceived(presentTime());
            return;
        }

        double nextDelay;
        do {
            nextDelay = rng.sample() / 1000;
        } while (nextDelay < 0); //ensures a positve delay, due to "infinte" gaussian deviation

        nextDelay =0; //TODO remove

        receiverEvent = new NetworkRequestReceiveEvent(getModel(), String.format("Receiving of %s", request.getQuotedName()), traceIsOn());
        TimeInstant next_receiveTime = new TimeInstant(presentTime().getTimeAsDouble() + nextDelay);
        receiverEvent.schedule(request, next_receiveTime);//TODO: add network delay
        request.setReceiveEvent(receiverEvent);
    }


    public void cancel() {
        receiverEvent.cancel();
        //TODO: fire canceledEvent after network timeout
    }
}
