package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

public abstract class ServiceOwnedPattern extends Pattern {

    protected final Microservice owner;

    public ServiceOwnedPattern(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace);
        this.owner = owner;
    }

}
