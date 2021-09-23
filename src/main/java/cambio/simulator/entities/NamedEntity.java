package cambio.simulator.entities;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * Class that adds further options for the retrieving of names of {@link Entity}s. Specifically, it provides a plain
 * name for each entity, that does not contain the number assigned by DESMO-J. However, these plain names are not
 * guaranteed to be unique.
 *
 * <p>
 * Plain names should be used when it comes to generating new entity names based on other entity names to prevent chains
 * of unique identifiers.
 *
 * @author Lion Wagner
 */
public abstract class NamedEntity extends Entity {

    private String plainName;

    public NamedEntity(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
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
