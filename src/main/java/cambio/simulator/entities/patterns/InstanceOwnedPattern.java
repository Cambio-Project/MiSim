package cambio.simulator.entities.patterns;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.nparsing.adapter.JsonTypeName;
import desmoj.core.simulator.Model;

/**
 * Represents a resilience pattern that is owned by a {@link MicroserviceInstance}.
 *
 * <p>
 * Subclasses of this type will be automatically parsed from an architecture description and attached to their
 * owning instance  if their type name is set
 * via the {@link JsonTypeName} annotation.
 * For a successful parsing the subtype need to have a constructor with the following signature: <br>
 * <pre>
 * constructor({@link Model}, {@link String}, {@link Boolean}, {@link MicroserviceInstance})
 * </pre>
 *
 * <p>
 * An Object of the following type...
 * <pre>
 *     &#64;JsonTypeName("retry", alternativeNames="Retry")
 *     public class Retry extends InstanceOwnedPattern {
 *     }
 * </pre>
 *
 * <p>
 * ... will be attached to each {@link MicroserviceInstance} of a
 * {@link Microservice} if its architecture description contains:
 *
 * <pre>
 *     patterns: [
 *         {
 *           type: "retry",
 *           config: {
 *               ...
 *           }
 *         },
 *     ]
 * </pre>
 *
 * @author Lion Wagner
 * @see IPatternLifeCycleHooks
 */
public abstract class InstanceOwnedPattern extends NamedEntity implements IPatternLifeCycleHooks {

    protected final MicroserviceInstance owner;

    public InstanceOwnedPattern(Model model, String name, boolean showInTrace, MicroserviceInstance owner) {
        super(model, name, showInTrace);
        this.owner = owner;
    }

}
