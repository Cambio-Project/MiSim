package cambio.simulator.orchestration.parsing;

public class MetadataDto {
    private String name;
    private String UID;
    private String namespace;
    private LabelsDto labels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public LabelsDto getLabels() {
        return labels;
    }

    public void setLabels(LabelsDto labels) {
        this.labels = labels;
    }
}
