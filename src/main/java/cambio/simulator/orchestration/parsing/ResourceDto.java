package cambio.simulator.orchestration.parsing;

public class ResourceDto {
    private String name;
    private TargetDto target;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TargetDto getTarget() {
        return target;
    }

    public void setTarget(TargetDto target) {
        this.target = target;
    }
}
