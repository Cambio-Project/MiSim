package cambio.simulator.events;

import cambio.tltea.interpreter.nodes.consequence.activation.HookEventData;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * An event without any direct effect on the simulation. However, listeners on the event bus can detect it and
 * execute further actions. It is associated to the {@link HookEventData}.
 */
public class HookEvent extends SelfScheduledExperimentAction {
    private final HookEventData data;
    private final boolean value;

    /**
     * Creates a new hook event.
     *
     * @param owner       The model that owns this event
     * @param data        the data of this hook event required to identify it.
     * @param value       whether the event is activated or deactivated.
     * @param showInTrace declaration whether this event should be shown in the trace
     */
    public HookEvent(Model owner, HookEventData data, boolean value, boolean showInTrace) {
        super(owner, "named-event:" + data.getEventName(), showInTrace);
        this.data = data;
        this.value = value;
    }

    @Override
    public void onRoutineExecution() throws SuspendExecution {
        // do nothing; just for listeners on the event bus
    }

    public HookEventData getData() {
        return data;
    }

    public boolean getValue() {
        return value;
    }
}
