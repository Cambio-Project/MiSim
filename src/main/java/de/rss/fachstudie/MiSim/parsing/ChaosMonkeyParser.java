package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.events.ChaosMonkeyEvent;
import de.rss.fachstudie.MiSim.misc.Util;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
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

        ChaosMonkeyEvent monkeyEvent = new ChaosMonkeyEvent(model, String.format("ChaosMonkey_(%s)", microservice), model.traceIsOn(), target, instances);
        monkeyEvent.setTargetTime(new TimeInstant(time, model.getExperiment().getReferenceUnit()));
        return monkeyEvent;
    }
}
