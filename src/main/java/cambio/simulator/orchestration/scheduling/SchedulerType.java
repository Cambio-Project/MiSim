package cambio.simulator.orchestration.scheduling;

public enum SchedulerType {


    FIRSTFIT("firstFit");

    SchedulerType(String name) {
        this.name = name;
    }
    String name;

    public String getName() {
        return name;
    }

    public static SchedulerType fromString(String name) {
        for (SchedulerType schedulerType : SchedulerType.values()) {
            if (schedulerType.getName().equalsIgnoreCase(name)) {
                return schedulerType;
            }
        }
        return null;
    }
}
