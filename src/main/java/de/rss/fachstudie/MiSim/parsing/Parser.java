package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.models.ArchitectureModel;
import desmoj.core.simulator.Model;

/**
 * Abstract superclass with a generic interface to ease object parsing.
 * <p>
 * TODO: create a Parser subclass for parsing time dependent events (e.g. SummonerMonkey, ChaosMonkey)
 *
 * @author Lion Wagner
 */
public abstract class Parser<T> {

    public abstract T convertToObject(Model model);

    protected Microservice getMircoserviceFromName(String name) {
        Microservice service = ArchitectureModel.get().getMicroservices().stream().filter(microservice -> microservice.getName().equals(name)).findAny().orElse(null);
        if (service == null) {
            throw new ParsingException(String.format("Could not find microservice with the name '%s'", name));
        }
        return service;
    }

    protected Operation getOperationFromName(String name, Microservice parent) {
        Operation targetOperation = parent.getOperationByName(name);
        if (targetOperation == null) {
            throw new ParsingException(String.format("Operation '%s' is not part of microserivce %s", name, parent.getName()));
        }
        return targetOperation;
    }

    public abstract String getDescriptionKey();
}
