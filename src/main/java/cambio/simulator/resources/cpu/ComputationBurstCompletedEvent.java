package cambio.simulator.resources.cpu;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.entities.networking.Request;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

/**
 * Event that represents the completion of a process burst for a specific {@link CPUProcess}.
 *
 * <p>
 * Automatically notifies its current CPU that this burst finished, so it can continue with the next burst.
 *
 * <p>
 * Fires a {@link ComputationCompletedEvent} automatically if the {@link CPUProcess} was finished with the current
 * burst.
 *
 * @author Lion Wagner
 */
public class ComputationBurstCompletedEvent extends NamedExternalEvent {
    private final CPUProcess endingProcess;
    private final CPU owner;
    private final int completedDemand;

    /**
     * Constructs a new {@link ComputationBurstCompletedEvent}.
     *
     * @param endingProcess   {@link CPUProcess} that ends with this burst
     * @param owner           {@link CPU} that is handling the calculation
     * @param completedDemand demand that was completed by the computation burst.
     */
    public ComputationBurstCompletedEvent(Model model, String name, boolean showInTrace, CPUProcess endingProcess,
                                          CPU owner, int completedDemand) {
        super(model, name, showInTrace);
        this.endingProcess = endingProcess;
        this.owner = owner;
        this.completedDemand = completedDemand;
        this.endingProcess.setCurrentBurstCompletionEvent(this);
    }

    @Override
    public void onRoutineExecution() throws SuspendExecution {
        synchronized (NamedSimProcess.class){

        endingProcess.reduceDemandRemainder(completedDemand);

        //notify cpu that the process finished its current burst
        owner.onBurstFinished(endingProcess);

        if (endingProcess.getDemandRemainder() <= 0) {
            //notify the request that its computation finished
            Request request = endingProcess.getRequest();
            ComputationCompletedEvent completionEvent = new ComputationCompletedEvent(getModel(),
                "ComputationEnd " + request.getQuotedPlainName(),
                getModel().traceIsOn());
            completionEvent.schedule(request, presentTime());
        }

        }
    }
}
