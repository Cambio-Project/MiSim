package de.rss.fachstudie.MiSim.parsing;

import de.rss.fachstudie.MiSim.entities.Dependency;
import de.rss.fachstudie.MiSim.entities.Operation;
import de.rss.fachstudie.MiSim.entities.microservice.Microservice;
import de.rss.fachstudie.MiSim.events.LatencyMonkeyEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

import java.util.Arrays;
import java.util.Set;

/**
 * @author Lion Wagner
 */
public final class LatencyMonkeyParser extends Parser<LatencyMonkeyEvent> {
    public Double time;
    public double delay;
    public double std_deviation = 0;
    public double duration = 0;

    //specification levels
    public String microservice;
    public String operation_src = null;
    public String operation_trg = null;


    @Override
    public LatencyMonkeyEvent convertToObject(Model model, Set<Microservice> microservices) {
        try {
            return parse(model, microservices);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Could not parse latency injector.", e);
        }
    }

    private LatencyMonkeyEvent parse(Model model, Set<Microservice> microservices) throws IllegalArgumentException {
        Microservice service = getMircoserviceFromName(microservice, microservices);


        if (time == null) {
            throw new ParsingException("Target time was not set.");
        }


        Operation src_op = service.getOperation(operation_src);
        Operation trg_op =
                src_op == null ?
                        null :
                        Arrays.stream(src_op.getDependencies()).map(Dependency::getTargetOperation).filter(targetOperation -> targetOperation.getName().equals(operation_trg)).findAny().orElse(null);

        LatencyMonkeyEvent event = new LatencyMonkeyEvent(model, generateName(), model.traceIsOn(), delay, std_deviation, service, src_op, trg_op);
        event.setTargetTime(new TimeInstant(time, model.getExperiment().getReferenceUnit()));

        return event;
    }

    private String generateName() {
        StringBuilder b = new StringBuilder("Lantency_Injector_");

        if (operation_src == null && operation_trg == null) {
            b.append(String.format("[%s]", microservice));
        } else if (operation_src != null && operation_trg == null) {
            b.append(String.format("[%s(%s)]", microservice, operation_src));
        } else if (operation_src != null && operation_trg != null) {
            b.append(String.format("[%s(%s)->(%s)]", microservice, operation_src, operation_trg)); //maybe TODO: find parent service of operation_trg
        }
        return b.toString();
    }
}
