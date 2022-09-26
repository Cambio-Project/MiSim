package cambio.simulator.entities.generator;

import cambio.simulator.entities.NamedExternalEvent;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;

/**
 * @author Lion Wagner
 */
public class ScaleLoadEvent extends NamedExternalEvent {

    private final LoadGeneratorDescriptionExecutor executor;
    private final double scaleFactor;
    private final TimeSpan duration;
    private final boolean isRevert;

    /**
     * Constructs a new named event.
     *
     * @param model       The model this event belongs to.
     * @param name        The name of this event.
     * @param showInTrace Flag indicating whether the entity should be shown in the trace.
     */
    public ScaleLoadEvent(Model model, String name, boolean showInTrace, LoadGeneratorDescriptionExecutor executor,
                          double scaleFactor, TimeSpan duration) {
        this(model, name, showInTrace, executor, scaleFactor, duration, false);
    }

    public ScaleLoadEvent(Model model, String name, boolean showInTrace, LoadGeneratorDescriptionExecutor executor,
                          double scaleFactor, TimeSpan duration, boolean isRevert) {
        super(model, name, showInTrace);
        this.executor = executor;
        this.scaleFactor = scaleFactor;
        this.duration = duration;
        this.isRevert = isRevert;
    }

    @Override
    public void onRoutineExecution() throws SuspendExecution {
        executor.scaleLoad(scaleFactor);

        if (!isRevert && duration != null && !duration.isZero() && Double.isFinite(duration.getTimeAsDouble())) {
            var revertEvent = new ScaleLoadEvent(getModel(),
                "Reverting " + getName(), this.traceIsOn(), executor,
                1 / scaleFactor, duration, true);
            revertEvent.schedule(duration);
            if (duration.getTimeInEpsilon() == 0) {
                this.sendWarning("Found suspicious State.", this.getClass().getSimpleName(),
                    "ScaleLoadEvent with duration 0 will never take effect", "Check your MTL input.");
            }
        }
    }
}
