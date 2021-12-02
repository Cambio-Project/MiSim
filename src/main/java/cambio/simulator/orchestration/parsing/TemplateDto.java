package cambio.simulator.orchestration.parsing;

public class TemplateDto {
    private MetadataDto metadata;
    private SpecContainerDto spec;

    public MetadataDto getMetadata() {
        return metadata;
    }

    public void setMetadata(MetadataDto metadata) {
        this.metadata = metadata;
    }

    public SpecContainerDto getSpec() {
        return spec;
    }

    public void setSpec(SpecContainerDto spec) {
        this.spec = spec;
    }
}
