package de.rss.fachstudie.MiSim.parsing;

import java.util.Arrays;

import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.entities.microservice.Operation;
import de.rss.fachstudie.MiSim.entities.networking.Dependency;
import de.rss.fachstudie.MiSim.events.LatencyMonkeyEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * Parser for a {@link LatencyMonkeyEvent}.
 *
 * @author Lion Wagner
 */
public final class LatencyMonkeyParser extends Parser<LatencyMonkeyEvent> {
    public Double time;
    public double delay;
    public double stdDeviation = 0;
    public double duration = 0;

    //specification levels
    public String microservice;
    public String operationSrc = null;
    public String operationTrg = null;


    @Override
    public LatencyMonkeyEvent convertToObject(Model model) {
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

    private LatencyMonkeyEvent parse(Model model) throws IllegalArgumentException {
        Microservice service = getMircoserviceFromName(microservice);


        if (time == null) {
            throw new ParsingException("Target time was not set.");
        }


        Operation srcOp = service.getOperationByName(operationSrc);
        Operation trgOp =
            srcOp == null ? null :
                Arrays.stream(srcOp.getDependencies()).map(Dependency::getTargetOperation)
                    .filter(targetOperation -> targetOperation.getName().equals(operationTrg)).findAny().orElse(null);

        LatencyMonkeyEvent event =
            new LatencyMonkeyEvent(model, generateName(), model.traceIsOn(), delay, stdDeviation, service, srcOp,
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
