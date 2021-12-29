package cambio.simulator.orchestration.loadbalancing;

public enum LoadBalancerType {


    RANDOM("random_orchestration", "RandomLoadBalancer"),
    LEAST_UTIL("leastUtil_orchestration", "LeastUtilBalancer");


    LoadBalancerType(String configName, String displayName) {
        this.configName = configName;
        this.displayName = displayName;
    }
    final String configName;
    final String displayName;

    public String getConfigName() {
        return configName;
    }

    public static LoadBalancerType fromString(String name) {
        for (LoadBalancerType loadBalancerType : LoadBalancerType.values()) {
            if (loadBalancerType.getConfigName().equalsIgnoreCase(name)) {
                return loadBalancerType;
            }
        }
        return null;
    }

    public String getDisplayName() {
        return displayName;
    }
}
