package cambio.simulator.orchestration.parsing;

public class MetricDto {
    private String type;
    private ResourceDto resource;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ResourceDto getResource() {
        return resource;
    }

    public void setResource(ResourceDto resource) {
        this.resource = resource;
    }
}
