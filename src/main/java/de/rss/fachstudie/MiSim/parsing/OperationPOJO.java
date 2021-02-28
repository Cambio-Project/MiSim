package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import de.rss.fachstudie.MiSim.models.MainModel;
import desmoj.core.simulator.Model;

/**
 * POJO that can hold the data about an Operation read from an architecture file.
 *
 * @author Lion Wagner
 */
public class OperationPOJO {
    public String name = "";
    public int demand = 0;
    public CircuitBreaker circuitBreaker = null;
    public Dependency[] dependencies = null;

    public Operation convertToOperation(Model model, boolean showInTrace, Microservice owner) {
        Operation op = new Operation(model, String.format("%s_%s", owner.getName(), name), showInTrace);
        op.setOwner(owner);
        op.setDemand(demand);
        op.setCircuitBreaker(circuitBreaker);
        op.setDependencies(dependencies);
        return op;
    }

}
