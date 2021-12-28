package cambio.simulator.orchestration.scaling;

import cambio.simulator.entities.NamedEntity;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;

public abstract class AutoScaler extends NamedEntity {
    protected double holdTimeUp;
    protected double holdTimeDown;

    public AutoScaler() {
        super(ManagementPlane.getInstance().getModel(), "AutoScaler", ManagementPlane.getInstance().getModel().traceIsOn());
        holdTimeUp = 5;
        holdTimeDown = 5;
    }

    // TODO call this constructor when parsing
    public AutoScaler(double holdTimeUp, double holdTimeDown) {
        super(ManagementPlane.getInstance().getModel(), "AutoScaler", ManagementPlane.getInstance().getModel().traceIsOn());
        this.holdTimeDown = holdTimeDown;
        this.holdTimeUp = holdTimeUp;
    }

    public void apply(Deployment deployment){

    }

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
}
