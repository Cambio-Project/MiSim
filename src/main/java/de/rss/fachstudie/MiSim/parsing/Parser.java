package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

import java.util.Set;

/**
 * @author Lion Wagner
 */
public abstract class Parser<T> {

    public abstract T convertToObject(Model model, Set<Microservice> microservices);

    protected Microservice getMircoserviceFromName(String name, Set<Microservice> microservices) {
        Microservice service = microservices.stream().filter(microservice -> microservice.getName().equals(name)).findAny().orElse(null);
        if (service == null) {
            throw new ParsingException(String.format("Could not find microservice with the name '%s'", name));
        }
        return service;
    }

    protected Operation getOperationFromName(String name, Microservice parent) {
        Operation targetOperation = parent.getOperation(name);
        if (targetOperation == null) {
            throw new ParsingException(String.format("Operation '%s' is not part of microserivce %s", name, parent.getName()));
        }
        return targetOperation;
    }
}
