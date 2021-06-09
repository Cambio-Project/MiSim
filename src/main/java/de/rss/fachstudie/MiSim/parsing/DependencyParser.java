package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.entities.networking.Dependency;
import desmoj.core.simulator.Model;

/**
 * Parser for {@link Dependency} objects.
 *
 * @author Lion Wagner
 */
public class DependencyParser extends Parser<Dependency> {
    public String service;
    public String operation;
    public Double delay = null;
    public Double probability = 1.0;

    private transient Operation owner;

    public void setOwningOperation(Operation owner) {
        this.owner = owner;
    }

    @Override
    public Dependency convertToObject(Model model) {
        Microservice targetService = getMircoserviceFromName(service);
        Operation targetOp = getOperationFromName(operation, targetService);
        return new Dependency(owner, targetOp, probability, delay);
    }

    @Override
    public String getDescriptionKey() {
        return null; //TODO: Not needed, extract seperate Parser subclass for experiment description
    }
}
