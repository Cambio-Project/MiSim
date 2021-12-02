package cambio.simulator.orchestration.parsing;

public enum K8Kind {
    DEPLOYMENT(SpecDeploymentDto.class);

    public final Class clazz;

    K8Kind(Class clazz) {
        this.clazz = clazz;
    }
}
