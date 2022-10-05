package cambio.simulator.entities.generator;

import cambio.simulator.entities.NamedExternalEvent;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * Event that handles scaling of load.
 *
 * @author Lion Wagner
 */
public class ScaleLoadEvent extends NamedExternalEvent {

    private final LoadGeneratorDescriptionExecutor executor;
    private final ScaleFactor scaleFactor;
    private final TimeSpan duration;

    /**
     * Constructs a new named event.
     *
     * @param model       The model this event belongs to.
     * @param name        The name of this event.
     * @param showInTrace Flag indicating whether the entity should be shown in the trace.
     */
    public ScaleLoadEvent(Model model, String name, boolean showInTrace, LoadGeneratorDescriptionExecutor executor,
                          ScaleFactor scaleFactor, TimeSpan duration) {
        super(model, name, showInTrace);
        this.executor = executor;
        this.scaleFactor = scaleFactor;
        this.duration = duration;
    }

    @Override
    public void onRoutineExecution() throws SuspendExecution {
        executor.scaleLoad(scaleFactor);
    }

}
