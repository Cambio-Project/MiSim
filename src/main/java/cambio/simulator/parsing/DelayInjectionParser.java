package cambio.simulator.parsing;

import java.util.Arrays;

import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.entities.networking.DependencyDescription;
import cambio.simulator.events.DelayInjection;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Parser for a {@link DelayInjection}.
 *
 * @author Lion Wagner
 */
public final class DelayInjectionParser extends Parser<DelayInjection> {
    public Double time;
    public double delay;
    @SerializedName(value = "stdDeviation", alternate = "std_deviation")
    public double stdDeviation = 0;
    public double duration = 0;

    //specification levels
    public String microservice;
    @SerializedName(value = "operationSrc", alternate = "operation_src")
    public String operationSrc = null;
    @SerializedName(value = "operationTrg", alternate = "operation_trg")
    public String operationTrg = null;


    @Override
    public DelayInjection convertToObject(Model model) {
        try {
            return parse(model);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Could not parse latency injector.", e);
        }
    }

    @Override
    public String getDescriptionKey() {
        return "latencymonkeys";
    }

    @Override
    public String[] getAlternateKeys() {
        return new String[] {"latencyInjection"};
    }

    private DelayInjection parse(Model model) throws IllegalArgumentException {
        Microservice service = getMircoserviceFromName(microservice);


        if (time == null) {
            throw new ParsingException("Target time was not set.");
        }


        Operation srcOp = service.getOperationByName(operationSrc);
        Operation trgOp =
            srcOp == null ? null :
                Arrays.stream(srcOp.getDependencies()).map(DependencyDescription::getTargetOperation)
                    .filter(targetOperation -> targetOperation.getName().equals(operationTrg)).findAny().orElse(null);

        DelayInjection event =
            new DelayInjection(model, generateName(), model.traceIsOn(), delay, stdDeviation, service, srcOp,
                trgOp);
        event.setDuration(duration);
        event.setTargetTime(new TimeInstant(time, model.getExperiment().getReferenceUnit()));

        return event;
    }

    private String generateName() {
        StringBuilder b = new StringBuilder("Lantency_Injector_");

        if (operationSrc == null && operationTrg == null) {
            b.append(String.format("[%s]", microservice));
        } else if (operationSrc != null && operationTrg == null) {
            b.append(String.format("[%s(%s)]", microservice, operationSrc));
        } else if (operationSrc != null && operationTrg != null) {
            b.append(String.format("[%s(%s)->(%s)]", microservice, operationSrc,
                operationTrg)); //maybe TODO: find parent service of operation_trg
        }
        return b.toString();
    }
}
