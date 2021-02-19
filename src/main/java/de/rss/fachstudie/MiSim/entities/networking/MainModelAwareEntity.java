package de.rss.fachstudie.MiSim.entities.networking;

import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class MainModelAwareEntity extends Entity {

    protected final MainModel model;

    public MainModelAwareEntity(MainModel model, String s, boolean b) {
        super(model, s, b);
        this.model =model;
    }

    @Override
    public MainModel getModel() {
        return model;
    }
}
