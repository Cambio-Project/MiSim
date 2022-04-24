package cambio.simulator.orchestration.scheduling;

public enum SchedulerType {


    FIRSTFIT("firstFit", "FirstFitScheduler"),
    RANDOM("random", "RandomScheduler"),
    KUBE("kube", "KubeScheduler"),
    ROUNDROBIN("roundRobin", "RoundRobin");

    SchedulerType(String name, String displayName) {
        this.name = name;
        this.displayName = displayName;
    }
    final String name;
    final String displayName;

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
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
