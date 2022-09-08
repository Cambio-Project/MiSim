package cambio.simulator.entities.generator;

import static cambio.simulator.export.MiSimReporters.GENERATOR_REPORTER;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.*;
import cambio.simulator.entities.patterns.IPatternLifeCycleHooks;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.AccumulativeDataPointReporter;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import org.jetbrains.annotations.NotNull;

/**
 * Class that can execute a {@link LoadGeneratorDescription}.
 *
 * <p>
 * Reads the {@link ArrivalRateModel} of a {@link LoadGeneratorDescription} and processes all its entries. Per entry a
 * {@link UserRequest} will be sent to the defined endpoint. This is done until the description throws a {@link
 * LoadGeneratorStopException}.
 *
 * @author Lion Wagner
 * @see ArrivalRateModel
 * @see LoadGeneratorDescription
 */
public final class LoadGeneratorDescriptionExecutor extends RequestSender implements IRequestUpdateListener,
    ISelfScheduled, IPatternLifeCycleHooks {
    private final Model model;

    /**
     * Target Operation.
     */
    private final Operation targetOperation;

    /*
     * Reporters
     */
    private final AccumulativeDataPointReporter accReporter;
    private final LoadGeneratorDescription loadGeneratorDescription;

    /**
     * Creates a new {@link LoadGeneratorDescriptionExecutor} that wants to execute the given {@link
     * LoadGeneratorDescription}.
     *
     * @param model                    the underlying model
     * @param loadGeneratorDescription behavioral description of this load generator.
     */
    public LoadGeneratorDescriptionExecutor(Model model, @NotNull LoadGeneratorDescription loadGeneratorDescription) {
        super(model, loadGeneratorDescription.getName() != null
            ? loadGeneratorDescription.getName()
            : "Generator", true);

        this.model = model;
        this.loadGeneratorDescription = loadGeneratorDescription;
        this.targetOperation = loadGeneratorDescription.targetOperation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());

        String reportName = String
            .format("G[%s]_[%s(%s)]_", this.getClass().getSimpleName(), targetOperation.getOwnerMS().getPlainName(),
                targetOperation.getPlainName());
        accReporter = new AccumulativeDataPointReporter(reportName, model);


        addUpdateListener(this);
    }

    @Override
    public void doInitialSelfSchedule() {
        ISelfScheduled selfScheduled = new GeneratorDescriptionExecutorScheduler(getPlainName());
        selfScheduled.doInitialSelfSchedule();
        this.start();
    }

    private void sendNewUserRequest() {
        UserRequest request = new UserRequest(model,
            "UserRequest@[" + targetOperation.getFullyQualifiedPlainName() + "]", model.traceIsOn(), targetOperation);
        try {
            sendRequest("SendingUserRequest(" + request.getPlainName() + ")", request, targetOperation.getOwnerMS());
        } catch (NoInstanceAvailableException e) {
            onRequestFailed(request, presentTime(), RequestFailedReason.NO_INSTANCE_AVAILABLE);
        }
    }

    /**
     * Listener for the failure of the sending process. This could for example be due to the receiving service not being
     * available, the request being canceled or timed out. Provides a reference to the failed request.
     */
    @Override
    public boolean onRequestFailed(Request request, TimeInstant when, RequestFailedReason reason) {
        sendTraceNote("Arrival of Request " + request + " failed at " + when + ".");
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests, so they can be directly compared
        accReporter.addDatapoint("SuccessfulRequests", currentTime, 0);

        GENERATOR_REPORTER.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests, so they can be directly compared
        GENERATOR_REPORTER.addDatapoint("SuccessfulRequests", currentTime, 0);

        return true;
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     */
    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        sendTraceNote("Successfully completed Request " + request + " at " + when + ".");
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
        //also creates a datapoint for failed requests, so they can be directly compared
        accReporter.addDatapoint("FailedRequests", currentTime, 0);


        GENERATOR_REPORTER.addDatapoint("FailedRequests", currentTime, 0);
        //also creates a datapoint for successful requests, so they can be directly compared
        GENERATOR_REPORTER.addDatapoint("SuccessfulRequests", currentTime, 1);
        return true;
    }

    private final class GeneratorDescriptionExecutorScheduler extends NamedSimProcess implements ISelfScheduled {

        private GeneratorDescriptionExecutorScheduler(String plainName) {
            super(model, plainName + "_Scheduler",
                true, true);
        }

        @Override
        public void lifeCycle() throws SuspendExecution {
            sendNewUserRequest();
            accReporter.addDatapoint("Load", presentTime(), 1);
            try {
                TimeInstant next = loadGeneratorDescription.getNextTimeInstant(presentTime());
                this.hold(next);
            } catch (LoadGeneratorStopException e) {
                model.sendTraceNote(String.format("Generator %s has stopped: %s", getName(), e.getMessage()));
                this.passivate();
            }
        }

        @Override
        public void doInitialSelfSchedule() {
            try {
                TimeInstant nextTimeInstant = loadGeneratorDescription.getInitialArrivalTime();
                this.activate(nextTimeInstant);
            } catch (LoadGeneratorStopException e) {
                sendWarning(String.format("Generator %s did not start.", this.getName()),
                    this.getClass().getCanonicalName(), e.getMessage(),
                    "Check your request generators definition and input for errors.");
            }
        }
    }
}
