package cambio.simulator.export;

import java.util.Objects;

import cambio.simulator.models.MiSimModel;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public abstract class MiSimReporter {

    protected final MiSimModel model;

    public MiSimReporter(Model model) {
        Objects.requireNonNull(model);
        this.model = (MiSimModel) model;
        MiSimReporters.registerReporter(this);
    }


    public abstract void finalizeReport();

    protected final <T> void checkArgumentsAreNotNull(String dataSetName, TimeInstant when, T data) {
        Objects.requireNonNull(dataSetName);
        Objects.requireNonNull(when);
        Objects.requireNonNull(data);
    }

    protected final boolean deregister() {
        return MiSimReporters.deregister(this);
    }
}
