package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;

import java.util.Random;

public abstract class AutoScaler extends NamedEntity {
    protected double holdTimeUp;
    protected double holdTimeDown;
    protected Random random;

    public AutoScaler() {
        super(ManagementPlane.getInstance().getModel(), "AutoScaler", ManagementPlane.getInstance().getModel().traceIsOn());
        holdTimeUp = 5;
        holdTimeDown = 5;
    }

    public abstract void apply(Deployment deployment);

    public double getHoldTimeUp() {
        return holdTimeUp;
    }

    public void setHoldTimeUp(double holdTimeUp) {
        this.holdTimeUp = holdTimeUp;
    }

    public double getHoldTimeDown() {
        return holdTimeDown;
    }

    public void setHoldTimeDown(double holdTimeDown) {
        this.holdTimeDown = holdTimeDown;
    }

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
