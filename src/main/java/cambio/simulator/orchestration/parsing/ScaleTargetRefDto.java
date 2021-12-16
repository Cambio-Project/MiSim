package cambio.simulator.orchestration.parsing;

public class ScaleTargetRefDto {
    // TODO go through each Dto and check if it only used by one other class if so, maybe make internal class
    private String apiVersion;
    private String kind;
    private String name;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
