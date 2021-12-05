package cambio.simulator.orchestration.parsing;

public class TargetDto {
    private String type;
    private Double averageUtilization;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Double getAverageUtilization() {
        return averageUtilization;
    }

    public void setAverageUtilization(Double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }
}
