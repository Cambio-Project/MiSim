package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.dist.DiscreteDist;

/**
 * @author Lion Wagner
 */
public class NetworkSendEvent extends ExternalTraceEvent {

    public static DiscreteDist<Long> delayGenerator = null;


    private final NetworkReceiveEvent receiveEvent;


    public NetworkSendEvent(MainModel model, String name) {
        super(model, "SenderEvent: " + name);
        receiveEvent = new NetworkReceiveEvent(model, name);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        receiveEvent.schedule(delayGenerator.sampleTimeSpan(model.getTimeUnit()));
    }

    public static void setDelay(DiscreteDist<Long> dist) {
        delayGenerator = dist;
    }
}
