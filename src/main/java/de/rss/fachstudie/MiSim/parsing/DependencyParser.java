package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

import java.util.Set;

/**
 * @author Lion Wagner
 */
public class DependencyParser extends Parser<Dependency> {
    public String service;
    public String operation;
    public Double delay = null;
    public Double probability = 1.0;

    private Operation owner;

    public void setOwningOperation(Operation owner) {
        this.owner = owner;
    }

    @Override
    public Dependency convertToObject(Model model, Set<Microservice> microservices) {
        Microservice targetMS = getMircoserviceFromName(service, microservices);
        Operation targetOp = getOperationFromName(operation, targetMS);
        return new Dependency(owner, targetOp, probability, delay);
    }
}
