package cambio.simulator.entities.patterns;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

/**
 * Resilience Pattern that is owned by a {@link Microservice}. These patterns should represent mechanics that live on a
 * meta-level, such as scaling.
 */
public abstract class ServiceOwnedPattern extends NamedEntity implements IPatternLifeCycleHooks {

    protected Microservice owner = null;

    public ServiceOwnedPattern(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

}
