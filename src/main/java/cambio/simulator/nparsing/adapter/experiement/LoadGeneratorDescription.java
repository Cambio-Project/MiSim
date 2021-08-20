package cambio.simulator.nparsing.adapter.experiement;

import cambio.simulator.entities.microservice.Operation;

/**
 * @author Lion Wagner
 */
public abstract class LoadGeneratorDescription {
    private final double repetitionSkip = 0.0d;
    private final double arrivalTime = 0;
    private final boolean repeating = false;
    private String operation;

    private transient Operation targetOperation;

    protected abstract ArrivalRateModel createArrivalRateModel();

    public final ArrivalRateModel getArrivalRateModel() {
        ArrivalRateModel model = createArrivalRateModel();
        model.setRepetitionDescription(new ArrivalRateModelRepetitionDescription(repeating, repetitionSkip));

//        this.targetOperation = NameResolver.resolveOperationName(operation);

        return model;
    }

    public final double getArrivalTime() {
        return arrivalTime;
    }


}
