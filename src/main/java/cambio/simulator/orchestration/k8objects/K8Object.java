package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.parsing.K8Kind;
import desmoj.core.simulator.Model;

/**
 * Abstraction layer if we want to add other objects than deployments
 */
public abstract class K8Object extends NamedEntity {
    public K8Kind kind;

    public K8Object(Model model, String name, boolean showInTrace, K8Kind kind) {
        super(model, name, showInTrace);
        this.kind = kind;
    }
}
