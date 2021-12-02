package cambio.simulator.orchestration.parsing;

import java.util.List;

public class ContainerDto {
    private String name;
    private String image;
    private List<PortsDto> ports;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<PortsDto> getPorts() {
        return ports;
    }

    public void setPorts(List<PortsDto> ports) {
        this.ports = ports;
    }
}
