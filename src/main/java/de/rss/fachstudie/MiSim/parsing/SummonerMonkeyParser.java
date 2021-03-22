package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.events.SummonerMonkeyEvent;
import de.rss.fachstudie.MiSim.misc.Util;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Set;

/**
 * @author Lion Wagner
 */
public class SummonerMonkeyParser extends Parser<SummonerMonkeyEvent> {
    public Double time;
    public int instances;

    //specification levels
    public String microservice;

    @Override
    public SummonerMonkeyEvent convertToObject(Model model, Set<Microservice> microservices) {
        try {
            return parse(model, microservices);
        } catch (Exception e) {
            throw new ParsingException("Could not parse a chaos monkey.", e);
        }
    }

    private SummonerMonkeyEvent parse(Model model, Set<Microservice> microservices) {
        if (time == null) {
            throw new ParsingException("Target time was not set.");
        }
        Util.requireNonNegative(instances, "Number of given instances cannot be smaller than 0.");

        Microservice target = getMircoserviceFromName(microservice, microservices);

        SummonerMonkeyEvent monkeyEvent = new SummonerMonkeyEvent(model, String.format("ChaosMonkey_(%s)", microservice), model.traceIsOn(), target, instances);
        monkeyEvent.setTargetTime(new TimeInstant(time, model.getExperiment().getReferenceUnit()));
        return monkeyEvent;
    }
}
