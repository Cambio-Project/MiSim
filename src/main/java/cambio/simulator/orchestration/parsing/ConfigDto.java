package cambio.simulator.orchestration.parsing;

import java.util.List;

public class ConfigDto {

    public Nodes nodes;
    public Scaler scaler;

    public class Nodes{
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

    public class Scaler{
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
}
