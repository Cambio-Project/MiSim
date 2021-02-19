package de.rss.fachstudie.MiSim.entities.networking;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class MainModelAwareRequestEvent extends Event<Request> {
    protected final MainModel model;


    public MainModelAwareRequestEvent(MainModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.model = model;
    }

    public final MainModel getModel(){
        return model;
    }

}
