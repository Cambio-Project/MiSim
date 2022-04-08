package cambio.simulator.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.models.MiSimModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;
import org.jetbrains.annotations.Contract;

/**
 * Represents an action during an experiment e.g. the attacking of a service or injection of network delay.
 *
 * @author Lion Wagner
 */
public abstract class ExperimentAction extends NamedExternalEvent implements ISelfScheduled {

    @Expose
    @SerializedName(value = "start", alternate = {"initial_arrival_time", "arrival_time", "time"})
    protected double initialArrivalTime = 0;

    public ExperimentAction(MiSimModel model, String s, boolean b) {
        super(model, s, b);
    }


    @Contract(pure = true)
    public double getInitialArrivalTime() {
        return initialArrivalTime;
    }
}
