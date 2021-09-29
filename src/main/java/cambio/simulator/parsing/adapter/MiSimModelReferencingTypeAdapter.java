package cambio.simulator.parsing.adapter;

import cambio.simulator.models.MiSimModel;
import com.google.gson.TypeAdapter;

/**
 * Collection class to mark Adapters that need a {@link MiSimModel} for parsing.
 *
 * @author Lion Wagner
 */
public abstract class MiSimModelReferencingTypeAdapter<T> extends TypeAdapter<T> {
    protected final MiSimModel model;

    public MiSimModelReferencingTypeAdapter(MiSimModel model) {
        this.model = model;
    }
}
