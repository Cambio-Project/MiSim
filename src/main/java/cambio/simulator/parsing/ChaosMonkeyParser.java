package cambio.simulator.parsing;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.misc.Util;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Parser vor a {@link ChaosMonkeyEvent}.
 *
 * @author Lion Wagner
 */
public class ChaosMonkeyParser extends Parser<ChaosMonkeyEvent> {
    public Double time;
    public int instances;

    //specification levels
    public String microservice;

    @Override
    public ChaosMonkeyEvent convertToObject(Model model) {
        try {
            return parse(model);
        } catch (Exception e) {
            throw new ParsingException("Could not parse a chaos monkey.", e);
        }
    }

    @Override
    public String getDescriptionKey() {
        return "chaosmonkeys";
    }

    private ChaosMonkeyEvent parse(Model model) {
        if (time == null) {
            throw new ParsingException("Target time was not set.");
        }
        Util.requireNonNegative(instances, "Number of given instances cannot be smaller than 0.");

        Microservice target = getMircoserviceFromName(microservice);

        ChaosMonkeyEvent monkeyEvent =
            new ChaosMonkeyEvent(model, String.format("ChaosMonkey_(%s)", microservice), model.traceIsOn(), target,
                instances);
        monkeyEvent.setTargetTime(new TimeInstant(time, model.getExperiment().getReferenceUnit()));
        return monkeyEvent;
    }
}
