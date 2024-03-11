package cambio.simulator.entities;

import cambio.simulator.EventBus;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * Class that adds further options for the retrieving of names of {@link ExternalEvent}s. Specifically, it provides a
 * plain name for each event, that does not contain the number assigned by DESMO-J. However, these plain names are not
 * guaranteed to be unique.
 *
 * <p>
 * Also pre-computes the names of the entity so the strings are not generated on every method call. This gives a
 * performance improvement over the default implementation when read a large amount of names.
 * </p>
 *
 * <p>
 * Plain names should be used when it comes to generating new entity names based on other entity names to prevent chains
 * of unique identifiers.
 *
 * @author Lion Wagner
 */
public abstract class NamedExternalEvent extends ExternalEvent {

    private String plainName;
    private String quotedName;
    private String quotedPlainName;

    /**
     * Constructs a new named event.
     *
     * @param model       The model this event belongs to.
     * @param name        The name of this event.
     * @param showInTrace Flag indicating whether the entity should be shown in the trace.
     */
    public NamedExternalEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.plainName = name;
        this.quotedPlainName = "'" + name + "'";
        this.quotedName = super.getQuotedName();
    }

    public String getPlainName() {
        return this.plainName;
    }

    /**
     * Renames this entity.
     */
    @Override
    public void rename(String name) {
        super.rename(name);
        this.plainName = name;
        this.quotedPlainName = "'" + name + "'";
        this.quotedName = super.getQuotedName();
    }

    /**
     * Gets a quoted version of the plain name of this object.
     *
     * @return the plain name of this entity surrounded with ' quotes.
     */
    public String getQuotedPlainName() {
        return this.quotedPlainName;
    }

    /**
     * Gets a quoted version of the name of this object. The name will include the object number assigned by DESMO-J.
     *
     * @return the name of this entity surrounded with ' quotes.
     */
    @Override
    public String getQuotedName() {
        return this.quotedName;
    }

    @Override
    public final void eventRoutine() throws SuspendExecution {
        EventBus.post(this);
        this.onRoutineExecution();
    }

    public abstract void onRoutineExecution() throws SuspendExecution;
}
