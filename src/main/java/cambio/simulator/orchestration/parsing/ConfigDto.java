package cambio.simulator.orchestration.parsing;

import java.util.List;

public class ConfigDto {

    public Nodes nodes;
    public Scaler scaler;
    public String loadBalancer;
    public String scheduler;
    public int healthCheckInterval;
    public List<CustomNodes> customNodes;
    public List<SchedulerPrio> schedulerPrio;
    public List<StartUpTimeContainer> startUpTimeContainer;

    public static class CustomNodes {
        String name;
        int cpu;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int cpu) {
            this.cpu = cpu;
        }
    }

    public static class SchedulerPrio {
        String name;
        int prio;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getPrio() {
            return prio;
        }

        public void setPrio(int prio) {
            this.prio = prio;
        }
    }

    public static class StartUpTimeContainer {
        String name;
        int time;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }
    }

    public class Nodes {
        int amount;
        int cpu;

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public int getCpu() {
            return cpu;
        }

        public void setCpu(int CPU) {
            this.cpu = CPU;
        }
    }

    public class Scaler {
        int holdTimeUpScaler;
        int holdTimeDownScaler;

        public int getHoldTimeUpScaler() {
            return holdTimeUpScaler;
        }

        public void setHoldTimeUpScaler(int holdTimeUpScaler) {
            this.holdTimeUpScaler = holdTimeUpScaler;
        }

        public int getHoldTimeDownScaler() {
            return holdTimeDownScaler;
        }

        public void setHoldTimeDownScaler(int holdTimeDownScaler) {
            this.holdTimeDownScaler = holdTimeDownScaler;
        }
    }

    public Nodes getNodes() {
        return nodes;
    }

    public void setNodes(Nodes nodes) {
        this.nodes = nodes;
    }

    public Scaler getScaler() {
        return scaler;
    }

    public void setScaler(Scaler scaler) {
        this.scaler = scaler;
    }

    public String getLoadBalancer() {
        return loadBalancer;
    }

    public void setLoadBalancer(String loadBalancer) {
        this.loadBalancer = loadBalancer;
    }

    public String getScheduler() {
        return scheduler;
    }

    public void setScheduler(String scheduler) {
        this.scheduler = scheduler;
    }

    public List<CustomNodes> getCustomNodes() {
        return customNodes;
    }

    public void setCustomNodes(List<CustomNodes> customNodes) {
        this.customNodes = customNodes;
    }

    public List<SchedulerPrio> getSchedulerPrio() {
        return schedulerPrio;
    }

    public void setSchedulerPrio(List<SchedulerPrio> schedulerPrio) {
        this.schedulerPrio = schedulerPrio;
    }

    public List<StartUpTimeContainer> getStartUpTimeContainer() {
        return startUpTimeContainer;
    }

    public void setStartUpTimeContainer(List<StartUpTimeContainer> startUpTimeContainer) {
        this.startUpTimeContainer = startUpTimeContainer;
    }

    public int getHealthCheckInterval() {
        return healthCheckInterval;
    }

    public void setHealthCheckInterval(int healthCheckInterval) {
        this.healthCheckInterval = healthCheckInterval;
    }
}
