package cambio.simulator.nparsing.adapter.experiement;

import java.io.File;

import cambio.simulator.nparsing.adapter.JsonTypeName;

/**
 * @author Lion Wagner
 */
@JsonTypeName(value = "limbo", alternativeNames = {"limbo_generator"})
public class LimboLoadGeneratorDescription extends LoadGeneratorDescription {

    private File model;

    @Override
    protected ArrivalRateModel createArrivalRateModel() {
        return null;
    }
}
