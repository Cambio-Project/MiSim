package de.unistuttgart.sqa.orcas.misim.entities.patterns;

import de.unistuttgart.sqa.orcas.misim.entities.microservice.Microservice;
import desmoj.core.simulator.Model;

/**
 * Resilience Pattern that is owned by a {@link Microservice}.
 * These patterns should represent mechanics that live on a meta-level, such as scaling.
 */
public abstract class ServiceOwnedPattern extends Pattern {

    protected final Microservice owner;

    public ServiceOwnedPattern(Model model, String name, boolean showInTrace, Microservice owner) {
        super(model, name, showInTrace);
        this.owner = owner;
    }

}
