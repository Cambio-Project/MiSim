package de.rss.fachstudie.MiSim.events;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;

/**
 * @author Lion Wagner
 */
public class NetworkReceiveEvent extends ExternalTraceEvent {
    public NetworkReceiveEvent(MainModel model, String name) {
        super(model, "ReceiverEvent " + name);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

    }
}
