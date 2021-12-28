package cambio.simulator.orchestration.parsing;

import java.util.List;

public class SpecHPADto {
    private ScaleTargetRefDto scaleTargetRef;
    private int minReplicas;
    private int maxReplicas;
    private List<MetricDto> metrics;

    public ScaleTargetRefDto getScaleTargetRef() {
        return scaleTargetRef;
    }

    public void setScaleTargetRef(ScaleTargetRefDto scaleTargetRef) {
        this.scaleTargetRef = scaleTargetRef;
    }

    public int getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(int minReplicas) {
        this.minReplicas = minReplicas;
    }

    public int getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(int maxReplicas) {
        this.maxReplicas = maxReplicas;
    }

    public List<MetricDto> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<MetricDto> metrics) {
        this.metrics = metrics;
    }


    public static class ScaleTargetRefDto {
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

    public static class MetricDto {
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

}
