package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.MicroserviceInstance;
import cambio.simulator.entities.microservice.NoInstanceAvailableException;

import java.util.Collection;

public interface ISchedulingStrategy {

    void schedulePod(Pod pod, Cluster cluster);
}



