package de.rss.fachstudie.MiSim.resources;

import co.paralleluniverse.fibers.SuspendExecution;
import de.rss.fachstudie.MiSim.entities.networking.Request;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * Event that represents the completion of a process burst for a specific {@link CPUProcess}.
 * <p>
 * Automatically notifies its current CPU that this burst finished, so it can continue with the next burst.
 * <p>
 * Fires a {@link ComputationCompletedEvent} automatically if the {@link CPUProcess} was finished with the current
 * burst.
 *
 * @author Lion Wagner
 */
public class ComputationBurstCompletedEvent extends ExternalEvent {
    private final CPUProcess ending_process;
    private final CPUImpl owner;
    private final int work_done;

    public ComputationBurstCompletedEvent(Model model, String name, boolean showInTrace, CPUProcess ending_process, CPUImpl owner, int work_done) {
        super(model, name, showInTrace);
        this.ending_process = ending_process;
        this.owner = owner;
        this.work_done = work_done;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {

        ending_process.reduceDemandRemainder(work_done);

        //notify cpu that the process finished its current burst
        owner.onBurstFinished(ending_process);

        if (ending_process.getDemandRemainder() <= 0) {
            //notify the request that its computation finished
            Request request = ending_process.getRequest();
            ComputationCompletedEvent completionEvent = new ComputationCompletedEvent(getModel(),
                    String.format("ComputationEnd %s", request.getQuotedName()),
                    getModel().traceIsOn());
            completionEvent.schedule(request, presentTime());
        }
    }
}
