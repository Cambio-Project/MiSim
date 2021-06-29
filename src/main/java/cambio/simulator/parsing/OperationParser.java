package cambio.simulator.parsing;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import desmoj.core.simulator.Model;

/**
 * POJO that can hold the data about an Operation read from an architecture file.
 *
 * @author Lion Wagner
 */
class OperationParser {
    public String name;
    public int demand;
    public DependencyParser[] dependencies = new DependencyParser[0];

    public Operation convertToOperation(Model model, boolean showInTrace, Microservice owner) {
        Operation op =
            new Operation(model, String.format("%s_(%s)", owner.getName(), name), showInTrace, owner, demand);
        op.setDependenciesData(dependencies);
        return op;
    }

}
