package cambio.simulator.entities;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;

/**
 * @author Lion Wagner
 */
public class NamedEntity extends Entity {

    private String plainName;

    public NamedEntity(Model model, String s, boolean b) {
        super(model, s, b);
        this.plainName = s;
    }

    public String getPlainName() {
        return getName().substring(0, getName().length() - ((int) Math.ceil(Math.log10(getIdentNumber()))));
    }

    @Override
    public void rename(String s) {
        super.rename(s);
        this.plainName = s;
    }

    public String getQuotedPlainName() {
        return "'" + getPlainName() + "'";
    }
}
