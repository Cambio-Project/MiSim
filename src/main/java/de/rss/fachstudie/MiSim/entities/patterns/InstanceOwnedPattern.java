package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

public abstract class InstanceOwnedPattern extends Pattern {

    protected final MicroserviceInstance owner;

    public InstanceOwnedPattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace);
        this.owner = owner;
    }

}
