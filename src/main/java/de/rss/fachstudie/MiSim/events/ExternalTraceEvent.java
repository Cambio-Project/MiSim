package de.rss.fachstudie.MiSim.events;

import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class ExternalTraceEvent extends ExternalEvent {

    protected final MainModel model;

    public ExternalTraceEvent(MainModel model, String name) {
        super(model, name, true);
        this.model = model;
    }
}
