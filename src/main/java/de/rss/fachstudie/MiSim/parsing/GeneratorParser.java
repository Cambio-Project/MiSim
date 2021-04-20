package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.generator.Generator;
import de.rss.fachstudie.MiSim.entities.generator.IntervalGenerator;
import de.rss.fachstudie.MiSim.entities.generator.LIMBOGenerator;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.misc.Util;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

import java.io.File;

/**
 * @author Lion Wagner
 */
class GeneratorParser extends Parser<Generator> {
    //General Properties
    public String microservice;
    public String operation;

    //TODO: Randomized Generator
    public ContDistUniform distributionWithin1s;

    //Interval Generator
    public Double interval;
    public double start = 0;

    //LIMBO Generator
    public String limbo_model;
    public boolean repeating = false;
    public double repetition_skip = 1000;


    @Override
    public Generator convertToObject(Model model) {

        Microservice service = super.getMircoserviceFromName(microservice);
        Operation targetOperation = service.getOperationByName(operation);


        if (limbo_model != null) {
            return new LIMBOGenerator(model, String.format("Limbo Generator [%s]", operation), model.traceIsOn(), targetOperation, new File(limbo_model), repeating, repetition_skip);
        } else if (interval != null) {
            Util.requireNonNegative(interval, "Interval cannot be negative.");
            return new IntervalGenerator(model, String.format("Interval Generator [%s]", operation), model.traceIsOn(), targetOperation, interval, start);
        }
        throw new ParsingException(String.format("Could not create a generator for %s. Could not figure out the generator type.", targetOperation.getQuotedName()));

    }

    @Override
    public String getDescriptionKey() {
        return "request_generators";
    }
}
