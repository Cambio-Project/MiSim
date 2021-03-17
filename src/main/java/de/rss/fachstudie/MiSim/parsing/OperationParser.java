package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.patterns.CircuitBreaker;
import desmoj.core.simulator.Model;

import java.util.Set;

/**
 * POJO that can hold the data about an Operation read from an architecture file.
 *
 * @author Lion Wagner
 */
class OperationParser{
    public String name;
    public int demand;
    public DependencyParser[] dependencies = new DependencyParser[0];
    //TODO: CircuitBreaker activation

    public Operation convertToOperation(Model model, boolean showInTrace, Microservice owner) {
        Operation op = new Operation(model, String.format("%s_(%s)", owner.getName(), name), showInTrace);
        op.setOwner(owner);
        op.setDemand(demand);
        op.setDependenciesData(dependencies);

        return op;
    }

}
