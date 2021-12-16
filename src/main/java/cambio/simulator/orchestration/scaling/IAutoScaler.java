package cambio.simulator.orchestration.scaling;

import cambio.simulator.orchestration.k8objects.Deployment;

public interface IAutoScaler {
    // TODO Scaler should be mapped to deployment
    void apply(Deployment deployment);
}
