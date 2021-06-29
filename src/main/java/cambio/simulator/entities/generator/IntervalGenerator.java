package cambio.simulator.entities.generator;

import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.UserRequest;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Generator that produces UserRequestArrivalEvent at a target Service Endpoint on an interval.
 *
 * <p>
 * This generator provides the following json options to the architecture:
 * <table border="1">
 *   <tr>
 *      <td>Name </td>      <td> Default Value </td> <td> Description</td>
 *   </tr>
 *   <tr>
 *      <td>interval</td>   <td>NONE(required)</td>  <td>Interval in ms to generate requests</td>
 *   </tr>
 *   <tr>
 *      <td> start </td>    <td>0</td>               <td>Starting time in ms of the generator.</td>
 *   </tr>
 *   <caption>Json properties of this generator.</caption>
 * </table>
 *
 * @author Lion Wagner TODO: even, exponential or randomized distribution within a simulation time unit
 */
public final class IntervalGenerator extends Generator {

    private final double interval;
    private final double start;

    //TODO: constructor for inter-arrival time

    /**
     * Constructs a new {@link IntervalGenerator} that starts immediately.
     *
     * @param model       handling DESMO-J model
     * @param name        name of the generator
     * @param showInTrace whether the sending of the {@link UserRequest} should be shown in the trace
     * @param operation   which {@link Operation} is the target of all {@link UserRequest}s
     * @param interval    With witch interval requests should be send.
     */
    public IntervalGenerator(Model model, String name, boolean showInTrace, Operation operation, double interval) {
        this(model, name, showInTrace, operation, interval, 0);
    }

    /**
     * Constructs a new {@link IntervalGenerator} that starts at a specific point in time.
     *
     * @param model       handling DESMO-J model
     * @param name        name of the generator
     * @param showInTrace whether the sending of the {@link UserRequest} should be shown in the trace
     * @param operation   which {@link Operation} is the target of all {@link UserRequest}s
     * @param interval    With witch interval requests should be send.
     */
    public IntervalGenerator(Model model, String name, boolean showInTrace, Operation operation, double interval,
                             double start) {
        super(model, name, showInTrace, operation);
        this.interval = interval; //TODO: Time Unit
        this.start = start;
    }

    @Override
    protected TimeInstant getNextTargetTime(final TimeInstant lastTargetTime) {
        double nextTargetTimeDouble = lastTargetTime != null ? lastTargetTime.getTimeAsDouble() : 0;
        return new TimeInstant(nextTargetTimeDouble + interval);
    }

    @Override
    protected TimeInstant getFirstTargetTime() {
        if (interval == 0) {
            throw new GeneratorStopException("Interval was not above 0.");
        }
        return new TimeInstant(start);
    }
}
