package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.MicroserviceInstance;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.dist.ContDistNormal;
import desmoj.core.dist.NumericalDist;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public class NetworkRequestSendEvent extends MainModelAwareRequestEvent {

    private final MicroserviceInstance sender;
    private NetworkRequestReceiveEvent receiverEvent;
    private final NumericalDist<Double> rng;

    public NetworkRequestSendEvent(MainModel model, String name, boolean showInTrace, MicroserviceInstance sender) {
        super(model, name, showInTrace);
        this.sender = sender;
        rng = new ContDistNormal(model, name + "_RNG", 20, 10, true, false);
    }


    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        //request is always send twice  sender->receiver->sender. we only need to stamp it the first time
        if (!request.isCompleted()) request.stampSendoff(presentTime());
        else if (!request.hasParent()) { // if it is completed and has no parent we are done
            request.stampReceived(presentTime());
            model.collector.reportRequestFinished(request);
            return;
        }

        Double nextDelay;
        do {
            nextDelay = rng.sample() / 1000;
        } while (nextDelay < 0); //ensures a positve delay, due to "infinte" gaussian deviation

        receiverEvent = new NetworkRequestReceiveEvent(getModel(), String.format("Receiving of %s", request.getQuotedName()), traceIsOn());
        TimeInstant next_receiveTime = new TimeInstant(presentTime().getTimeAsDouble() + nextDelay);
        receiverEvent.schedule(request, next_receiveTime);//TODO: add network delay
    }


    public void cancel() {
        receiverEvent.cancel();
        //TODO: fire canceledEvent after network timeout
    }
}
