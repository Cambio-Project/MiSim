package cambio.simulator.entities;

import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public abstract class NamedExternalEvent extends ExternalEvent {

    private String plainName;

    public NamedExternalEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    public String getPlainName() {
        return this.plainName;
    }

    @Override
    public void rename(String name) {
        super.rename(name);
        this.plainName = name;
    }

    /**
     * Gets a quoted version of the plain name of this object.
     *
     * @return the plain name of this entity surrounded with ' quotes.
     */
    public String getQuotedPlainName() {
        return "'" + getPlainName() + "'";
    }
}
