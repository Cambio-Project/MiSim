package cambio.simulator.entities.generator;

import cambio.simulator.entities.networking.IRequestUpdateListener;
import cambio.simulator.events.ISelfScheduled;
import cambio.simulator.nparsing.adapter.experiement.ArrivalRateModel;
import cambio.simulator.nparsing.adapter.experiement.LoadGeneratorDescription;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.core.simulator.TimeInstant;


/**
 * @author Lion Wagner
 */
public final class LoadGenerator extends SimProcess implements IRequestUpdateListener, ISelfScheduled {

    private final LoadGeneratorDescription description;
    private final ArrivalRateModel arrivalRateModel;

    public LoadGenerator(LoadGeneratorDescription description, Model model, String name, boolean showInTrace) {
        super(model, name, false, showInTrace);
        this.description = description;
        this.arrivalRateModel = description.getArrivalRateModel();
    }

    @Override
    public void doInitialSelfSchedule() {
        try {
            double firstTargetTime =
                description.getArrivalTime() + arrivalRateModel.getFirstTimeInstant().getTimeAsDouble();
            this.activate(new TimeInstant(firstTargetTime));

        } catch (GeneratorStopException e) {
            System.out
                .println("WARNING: Generator " + this.getName() + " was not started: no arrival rates were defined.");
        }
    }

    @Override
    public void lifeCycle() throws SuspendExecution {
        sendUserRequest();

        try {
            TimeInstant nextTargetTime = arrivalRateModel.getNextTimeInstant();
            this.hold(nextTargetTime);
        } catch (GeneratorStopException e) {
            e.printStackTrace();
            System.out
                .println("WARNING: Generator " + this.getName() + " was not started: no arrival rates were defined.");
        }

    }


    private void sendUserRequest() {

    }
}
