package cambio.simulator.orchestration.parsing;

import cambio.simulator.orchestration.environment.Node;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

            public Affinity affinity;

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

            public Affinity getAffinity() {
                return affinity;
            }

            public void setAffinity(Affinity affinity) {
                this.affinity = affinity;
            }

            public static class Affinity {
                private NodeAffinity nodeAffinity;

                public NodeAffinity getNodeAffinity() {
                    return nodeAffinity;
                }

                public void setNodeAffinity(NodeAffinity nodeAffinity) {
                    this.nodeAffinity = nodeAffinity;
                }
            }

            public static class ContainerDto {
                private String name;
                private String image;
                private List<PortsDto> ports;
                private ResourcesDto resources;

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

                public ResourcesDto getResources() {
                    return resources;
                }

                public void setResources(ResourcesDto resources) {
                    this.resources = resources;
                }

                private static class ResourcesDto {
                    private RequestsDto requests;

                    public RequestsDto getRequests() {
                        return requests;
                    }

                    public void setRequests(RequestsDto requests) {
                        this.requests = requests;
                    }
                    private static class RequestsDto {
                        private String cpu;

                        public String getCpu() {
                            return cpu;
                        }

                        public void setCpu(String cpu) {
                            this.cpu = cpu;
                        }
                    }

                }


                public static class PortsDto {
                    private String containerPort;

                    public String getContainerPort() {
                        return containerPort;
                    }

                    public void setContainerPort(String containerPort) {
                        this.containerPort = containerPort;
                    }
                }
            }

            public static class NodeAffinity{
                private RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution;

                public RequiredDuringSchedulingIgnoredDuringExecution getRequiredDuringSchedulingIgnoredDuringExecution() {
                    return requiredDuringSchedulingIgnoredDuringExecution;
                }

                public void setRequiredDuringSchedulingIgnoredDuringExecution(RequiredDuringSchedulingIgnoredDuringExecution requiredDuringSchedulingIgnoredDuringExecution) {
                    this.requiredDuringSchedulingIgnoredDuringExecution = requiredDuringSchedulingIgnoredDuringExecution;
                }

                public static class RequiredDuringSchedulingIgnoredDuringExecution {
                    private List<NodeSelectorTerms> nodeSelectorTerms;

                    public List<NodeSelectorTerms> getNodeSelectorTerms() {
                        return nodeSelectorTerms;
                    }

                    public void setNodeSelectorTerms(List<NodeSelectorTerms> nodeSelectorTerms) {
                        this.nodeSelectorTerms = nodeSelectorTerms;
                    }

                    public static class NodeSelectorTerms {
                        List<MatchExpressions> matchExpressions;

                        public List<MatchExpressions> getMatchExpressions() {
                            return matchExpressions;
                        }

                        public void setMatchExpressions(List<MatchExpressions> matchExpressions) {
                            this.matchExpressions = matchExpressions;
                        }

                        public static class MatchExpressions {
                            private String key;
                            private String operator;
                            private List<String> values;

                            public String getKey() {
                                return key;
                            }

                            public void setKey(String key) {
                                this.key = key;
                            }

                            public String getOperator() {
                                return operator;
                            }

                            public void setOperator(String operator) {
                                this.operator = operator;
                            }

                            public List<String> getValues() {
                                return values;
                            }

                            public void setValues(List<String> values) {
                                this.values = values;
                            }
                        }

                    }
                }


            }
        }
    }


}
