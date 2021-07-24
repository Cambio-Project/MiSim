package cambio.simulator.parsing;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import desmoj.core.simulator.Model;

/**
 * Parser for {@link DependencyDescription} objects.
 *
 * @author Lion Wagner
 */
public class DependencyParser extends Parser<DependencyDescription> {
    public String service;
    public String operation;
    public Double delay = null;
    public Double probability = 1.0;

    private transient Operation owner;

    public void setOwningOperation(Operation owner) {
        this.owner = owner;
    }

    @Override
    public DependencyDescription convertToObject(Model model) {
        Microservice targetService = getMircoserviceFromName(service);
        Operation targetOp = getOperationFromName(operation, targetService);
        return new DependencyDescription(owner, targetOp, probability, delay);
    }

    @Override
    public String getDescriptionKey() {
        return null; //TODO: Not needed, extract seperate Parser subclass for experiment description
    }
}
