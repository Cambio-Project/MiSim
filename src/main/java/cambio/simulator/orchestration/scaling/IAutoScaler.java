package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.k8objects.Deployment;

public interface IAutoScaler {

    void apply(Deployment deployment);
}
