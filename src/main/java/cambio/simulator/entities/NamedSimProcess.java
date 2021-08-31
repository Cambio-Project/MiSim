package cambio.simulator.entities;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;

/**
 * Class that adds further options for the retrieving of names of entities.
 * Specifically it provides a plain name for each entity that does not contain the number assinged by DesmoJ.
 * However, these plain names are not guaranteed to be unique.
 *
 * <p>
 * Plain names should be used when it comes to generating new entity names based on other entity names to prevent
 * chains of unique identifiers.
 *
 * @author Lion Wagner
 */
public abstract class NamedSimProcess extends SimProcess {

    private String plainName;

    public NamedSimProcess(Model model, String name, boolean showInTrace) {
        this(model, name, false, showInTrace);
    }

    public NamedSimProcess(Model model, String name, boolean repeating, boolean showInTrace) {
        super(model, name, repeating, showInTrace);
        this.plainName = name;
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
