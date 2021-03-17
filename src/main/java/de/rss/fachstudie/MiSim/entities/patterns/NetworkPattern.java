package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import de.rss.fachstudie.MiSim.entities.networking.IRequestUpdateListener;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class NetworkPattern extends Pattern implements IRequestUpdateListener {
    public NetworkPattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace, owner);
    }
}
