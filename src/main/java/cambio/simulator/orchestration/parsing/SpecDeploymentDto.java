package cambio.simulator.orchestration.parsing;

import java.util.List;

public class SpecDeploymentDto {
    private SelectorDto selector;
    private int replicas;
    private TemplateDto template;

    public SelectorDto getSelector() {
        return selector;
    }

    public void setSelector(SelectorDto selector) {
        this.selector = selector;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }

    public TemplateDto getTemplate() {
        return template;
    }

    public void setTemplate(TemplateDto template) {
        this.template = template;
    }

    public static class SelectorDto {

        private LabelsDto matchLabels;

        public LabelsDto getMatchLabels() {
            return matchLabels;
        }

        public void setMatchLabels(LabelsDto matchLabels) {
            this.matchLabels = matchLabels;
        }

    }

    public static class TemplateDto {
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

        public static class SpecContainerDto {

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

            public static class ContainerDto {
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

                public class PortsDto {
                    private String containerPort;

                    public String getContainerPort() {
                        return containerPort;
                    }

                    public void setContainerPort(String containerPort) {
                        this.containerPort = containerPort;
                    }
                }
            }
        }
    }


}
