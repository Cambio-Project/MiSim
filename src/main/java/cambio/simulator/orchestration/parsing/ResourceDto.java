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
}
