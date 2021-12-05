package cambio.simulator.orchestration.parsing;

public class K8HPADto extends K8ObjectDto{
    private SpecHPADto spec;

    public SpecHPADto getSpec() {
        return spec;
    }

    public void setSpec(SpecHPADto spec) {
        this.spec = spec;
    }
}
