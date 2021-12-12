package cambio.simulator.orchestration.parsing;

import java.util.List;

public class SpecContainerDto {

    public String schedulerName;

    public List<ContainerDto> containers;

    public List<ContainerDto> getContainers() {
        return containers;
    }

    public void setContainers(List<ContainerDto> containers) {
        this.containers = containers;
    }

    public String getSchedulerName() {
        return schedulerName;
    }

    public void setSchedulerName(String schedulerName) {
        this.schedulerName = schedulerName;
    }
}
