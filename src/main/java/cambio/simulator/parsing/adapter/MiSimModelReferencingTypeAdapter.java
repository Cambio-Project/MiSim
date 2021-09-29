package cambio.simulator.parsing.adapter;

import cambio.simulator.models.ExperimentModel;
import cambio.simulator.models.MiSimModel;
import com.google.gson.TypeAdapter;

/**
 * @author Lion Wagner
 */
public abstract class MiSimModelReferencingTypeAdapter<T> extends TypeAdapter<T> {
    protected final MiSimModel baseModel;

    public MiSimModelReferencingTypeAdapter(MiSimModel baseModel) {
        this.baseModel = baseModel;
    }
}
