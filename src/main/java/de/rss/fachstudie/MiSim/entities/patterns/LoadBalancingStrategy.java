package de.rss.fachstudie.MiSim.entities.patterns;

import de.rss.fachstudie.MiSim.entities.microservice.MicroserviceInstance;
import desmoj.core.simulator.Model;

import java.util.Collection;
import java.util.Locale;

public interface LoadBalancingStrategy {

    MicroserviceInstance getNextInstance(Collection<MicroserviceInstance> runningInstances);

    static LoadBalancingStrategy fromName(Model model, String name) {
        switch (String.valueOf(name).toLowerCase(Locale.ROOT)) {
            case "even":
                return new EvenLoadBalanceStrategy();
            case "random":
                return new RandomLoadBalanceStrategy();
            case "util":
                return new UtilizationBalanceStrategy();
            default:
                model.sendWarning("Defaulting to randomized load balancing.",
                        LoadBalancingStrategy.class.getTypeName(),
                        String.format("Could not find load balancing strategy \"%s\"", name),
                        "Use one of the existing Load balancing strategies.");
                return new RandomLoadBalanceStrategy();
        }
    }

}

