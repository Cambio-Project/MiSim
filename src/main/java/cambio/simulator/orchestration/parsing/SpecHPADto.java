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
}
