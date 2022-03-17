package cambio.simulator.orchestration.management;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.entities.NamedSimProcess;
import cambio.simulator.entities.generator.IntervalLoadGeneratorDescription;
import cambio.simulator.entities.generator.LoadGeneratorStopException;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.orchestration.events.ScaleEvent;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

public class ScaleTaskExecutor extends NamedEntity implements ISelfScheduled {
    private final Model model;
    IntervalLoadGeneratorDescription intervalLoadGeneratorDescription;

    public ScaleTaskExecutor(Model model, String name, boolean showInTrace, int interval) {
        super(model, name, showInTrace);
        this.model = model;
        intervalLoadGeneratorDescription = new IntervalLoadGeneratorDescription();
        intervalLoadGeneratorDescription.setInterval(interval);
        intervalLoadGeneratorDescription.initializeArrivalRateModel();

    }

    public void executeManagementTasks(){
        final ScaleEvent scaleEvent = new ScaleEvent(getModel(), "ScaleEvent", traceIsOn());
        scaleEvent.schedule(getModel().presentTime());
    }

    @Override
    public void doInitialSelfSchedule() {
        ISelfScheduled selfScheduled = new MasterTasksExecutorScheduler(getPlainName());
        selfScheduled.doInitialSelfSchedule();
    }


    private final class MasterTasksExecutorScheduler extends NamedSimProcess implements ISelfScheduled {

        private MasterTasksExecutorScheduler(String plainName) {
            super(model, plainName + "_Scheduler",
                    true, true);
        }

        @Override
        public void lifeCycle() throws SuspendExecution {
            executeManagementTasks();
            try {
                TimeInstant next = intervalLoadGeneratorDescription.getNextTimeInstant();
                this.hold(next);
            } catch (LoadGeneratorStopException e) {
                model.sendTraceNote(String.format("Generator %s has stopped: %s", getName(), e.getMessage()));
                this.passivate();
            }
        }

        @Override
        public void doInitialSelfSchedule() {
            try {
                TimeInstant nextTimeInstant = intervalLoadGeneratorDescription.getNextTimeInstant();
                this.activate(nextTimeInstant);
            } catch (LoadGeneratorStopException e) {
                sendWarning(String.format("Generator %s did not start.", this.getName()),
                        this.getClass().getCanonicalName(), e.getMessage(),
                        "Check your request generators definition and input for errors.");
            }
        }
    }

}
