package cambio.simulator.orchestration.parsing;

/**
 * Constants for k8s objects we can read from yaml files
 */
public enum K8Kind {
    DEPLOYMENT,
    HORIZONTALPODAUTOSCALER,
    RANDOMAUTOSCALER
}
