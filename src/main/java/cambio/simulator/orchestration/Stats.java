package cambio.simulator.orchestration;

import cambio.simulator.orchestration.k8objects.Deployment;
import java.util.*;

public class Stats {

    List<List<String>> dataLines = new ArrayList<>();

    Map<Deployment, List<ScalingRecord>> deploymentRecordsMap = new HashMap<>();

    public static class ScalingRecord {
        int time;
        double avgConsumption;
        int amountPods;

        public ScalingRecord(int time, double avgConsumption, int amountPods) {
            this.time = time;
            this.avgConsumption = avgConsumption;
            this.amountPods = amountPods;
        }
        public ScalingRecord(){}

        public int getTime() {
            return time;
        }

        public void setTime(int time) {
            this.time = time;
        }

        public double getAvgConsumption() {
            return avgConsumption;
        }

        public void setAvgConsumption(double avgConsumption) {
            this.avgConsumption = avgConsumption;
        }

        public int getAmountPods() {
            return amountPods;
        }

        public void setAmountPods(int amountPods) {
            this.amountPods = amountPods;
        }
    }

    private static final Stats instance = new Stats();

    //private constructor to avoid client applications to use constructor
    private Stats() {}

    public static Stats getInstance() {
        return instance;
    }


    public List<List<String>> getDataLines() {
        return dataLines;
    }

    public void setDataLines(List<List<String>> dataLines) {
        this.dataLines = dataLines;
    }

    public Map<Deployment, List<ScalingRecord>> getDeploymentRecordsMap() {
        return deploymentRecordsMap;
    }

    public void setDeploymentRecordsMap(Map<Deployment, List<ScalingRecord>> deploymentRecordsMap) {
        this.deploymentRecordsMap = deploymentRecordsMap;
    }
}
