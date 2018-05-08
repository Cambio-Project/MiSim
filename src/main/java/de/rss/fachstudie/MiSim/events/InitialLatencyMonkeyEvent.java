package de.rss.fachstudie.MiSim.events;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

public class InitialLatencyMonkeyEvent extends ExternalEvent {

    public InitialLatencyMonkeyEvent(Model owner, String name, boolean showInTrace, double time) {
        super(owner, name, showInTrace);
    }

    @Override
    public void eventRoutine() {
        // TODO implement
    }
}
