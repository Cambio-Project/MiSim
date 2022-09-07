package cambio.simulator.resources.cpu;

import cambio.simulator.entities.networking.Request;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Event;
import desmoj.core.simulator.Model;

/**
 * Marks the end of the last computation burst of a thread.
 *
 * <p>
 * On execution, it resubmits the computed request at its handler.
 *
 * @author Lion Wagner
 * @see ComputationBurstCompletedEvent
 * @see CPU
 * @see CPUProcess
 */
public class ComputationCompletedEvent extends Event<Request> {

    public ComputationCompletedEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
    }

    @Override
    public void eventRoutine(Request request) throws SuspendExecution {
        sendTraceNote("Request " + request.getQuotedName() + " was computed.");
        request.setComputationCompleted();
        request.getHandler().handle(request); //resubmitting itself for further handling
    }

}
