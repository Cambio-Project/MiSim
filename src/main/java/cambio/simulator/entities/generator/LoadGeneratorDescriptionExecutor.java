package cambio.simulator.entities.generator;

import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.IRequestUpdateListener;
import cambio.simulator.entities.networking.Request;
import cambio.simulator.entities.networking.RequestFailedReason;
import cambio.simulator.entities.networking.RequestSender;
import cambio.simulator.entities.networking.UserRequest;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.export.AccumulativeDataPointReporter;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * @author Lion Wagner
 */
public final class LoadGeneratorDescriptionExecutor extends RequestSender implements IRequestUpdateListener,
    ISelfScheduled {
    private static final AccumulativeDataPointReporter allReporter = new AccumulativeDataPointReporter("GEN_ALL");
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

    public LoadGeneratorDescriptionExecutor(Model model, LoadGeneratorDescription loadGeneratorDescription) {
        super(model, loadGeneratorDescription.getName() != null
            ? loadGeneratorDescription.getName()
            : "Generator", true);

        this.model = model;
        this.loadGeneratorDescription = loadGeneratorDescription;
        this.targetOperation = loadGeneratorDescription.targetOperation;
        super.sendTraceNote("starting Generator " + this.getQuotedName());

        String reportName = String
            .format("G[%s]_[%s(%s)]_", this.getClass().getSimpleName(), targetOperation.getOwnerMS().getName(),
                targetOperation.getName());
        accReporter = new AccumulativeDataPointReporter(reportName);


        addUpdateListener(this);
    }

    @Override
    public void doInitialSelfSchedule() {
        ISelfScheduled selfScheduled = new GeneratorDescriptionExecutorScheduler(getPlainName());
        selfScheduled.doInitialSelfSchedule();
    }

    private void sendNewUserRequest() {
        UserRequest request = new UserRequest(model, String.format("UserRequest@[%s]",
            targetOperation.getFullyQualifiedPlainName()), true, targetOperation);
        try {
            sendRequest(String.format("SendingUserRequest(%s)", request.getPlainName()), request,
                targetOperation.getOwnerMS());
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
        sendTraceNote(String.format("Arrival of Request %s failed at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests so they can be directly compared
        accReporter.addDatapoint("SuccessfulRequests", currentTime, 0);

        allReporter.addDatapoint("FailedRequests", currentTime, 1);
        //also creates a datapoint for successful requests so they can be directly compared
        allReporter.addDatapoint("SuccessfulRequests", currentTime, 0);

        return true;
    }

    /**
     * Listener for the successful receiving of the answer of a request.
     */
    @Override
    public boolean onRequestResultArrivedAtRequester(Request request, TimeInstant when) {
        sendTraceNote(String.format("Successfully completed Request %s at %s.", request, when));
        TimeInstant currentTime = new TimeInstant(Math.ceil(presentTime().getTimeAsDouble()));

        accReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
        //also creates a datapoint for failed requests so they can be directly compared
        accReporter.addDatapoint("FailedRequests", currentTime, 0);


        allReporter.addDatapoint("FailedRequests", currentTime, 0);
        //also creates a datapoint for successful requests so they can be directly compared
        allReporter.addDatapoint("SuccessfulRequests", currentTime, 1);
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
            try {
                TimeInstant next = loadGeneratorDescription.getNextTimeInstant();
                this.hold(next);
            } catch (LoadGeneratorStopException e) {
                model.sendTraceNote(String.format("Generator %s has stopped: %s", getName(), e.getMessage()));
                this.passivate();
            }
        }

        @Override
        public void doInitialSelfSchedule() {
            try {
                TimeInstant nextTimeInstant = loadGeneratorDescription.getNextTimeInstant();
                this.activate(nextTimeInstant);
            } catch (LoadGeneratorStopException e) {
                sendWarning(String.format("Generator %s did not start.", this.getName()),
                    this.getClass().getCanonicalName(), e.getMessage(),
                    "Check your request generators definition and input for errors.");
            }
//            catch (SuspendExecution e) {
//                e.fillInStackTrace().printStackTrace();
//                // may need to be rethrown. see. https://docs.paralleluniverse.co/quasar/javadoc/co/paralleluniverse/fibers/SuspendExecution.html
//            }
        }

        private void forceScheduleAt(TimeInstant targetTime) throws SuspendExecution {
            if (this.isScheduled()) {
                this.reSchedule(targetTime);
            } else {
                this.activate(targetTime);
            }
        }
    }
}
