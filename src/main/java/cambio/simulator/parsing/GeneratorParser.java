package cambio.simulator.parsing;

import java.io.File;

import cambio.simulator.entities.generator.Generator;
import cambio.simulator.entities.generator.IntervalGenerator;
import cambio.simulator.entities.generator.LIMBOGenerator;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.misc.Util;
import com.google.gson.annotations.SerializedName;
import desmoj.core.dist.ContDistUniform;
import desmoj.core.simulator.Model;

/**
 * Parser for a {@link Generator}.
 * Can distinguish between {@link LIMBOGenerator} and {@link IntervalGenerator}.
 *
 * @author Lion Wagner
 */
class GeneratorParser extends Parser<Generator> {
    //General Properties
    public String microservice;
    public String operation;

    //TODO: Randomized Generator
    public transient ContDistUniform distributionWithin1s;

    //Interval Generator
    public Double interval;
    public double start = 0;

    //LIMBO Generator
    @SerializedName(value = "limboModel", alternate = {"limbo_model"})
    public String limboModel;
    public boolean repeating = false;
    @SerializedName(value = "repetitionSkip", alternate = {"repetition_skip"})
    public double repetitionSkip = 1000;


    @Override
    public Generator convertToObject(Model model) {

        Microservice service = super.getMircoserviceFromName(microservice);
        Operation targetOperation = service.getOperationByName(operation);


        if (limboModel != null) {
            return new LIMBOGenerator(model, String.format("Limbo Generator [%s]", operation), model.traceIsOn(),
                targetOperation, new File(limboModel), repeating, repetitionSkip);
        } else if (interval != null) {
            Util.requireNonNegative(interval, "Interval cannot be negative.");
            return new IntervalGenerator(model, String.format("Interval Generator [%s]", operation), model.traceIsOn(),
                targetOperation, interval, start);
        }
        throw new ParsingException(String
            .format("Could not create a generator for %s. Could not figure out the generator type.",
                targetOperation.getQuotedName()));

    }

    @Override
    public String getDescriptionKey() {
        return "request_generators";
    }
}
