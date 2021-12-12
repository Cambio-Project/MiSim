package cambio.simulator.orchestration.k8objects;

import cambio.simulator.entities.NamedEntity;
import desmoj.core.simulator.Model;

public abstract class K8Object extends NamedEntity {
    public K8Kind kind;

    public K8Object(Model model, String name, boolean showInTrace, K8Kind kind) {
        super(model, name, showInTrace);
        this.kind = kind;
    }
}
