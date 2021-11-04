package desmoj.extensions.visualization3d;

import org.scijava.java3d.Alpha;
import org.scijava.java3d.RotationInterpolator;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;
import org.scijava.java3d.WakeupCriterion;
import org.scijava.java3d.WakeupOnElapsedFrames;
import java.util.Enumeration;

/**
 * A slightly modified version of RotationInterpolator. It doesn't need much rotational data at the initiation. The
 * alpha, rotation axis and the angles just need to be specified at the start. If the rotation is finished, the
 * calculation will be stopped and the alpha removed.
 *
 * @author Fred Sun
 */
public class RotationSwitchInterpolator extends RotationInterpolator {

    //saves the current transform information
    private Transform3D _currentTransform = new Transform3D();

    //two transform to help calculating the rotational interpolation
    private final Transform3D _rotationTransform = new Transform3D();
    private final Transform3D _angleTransform = new Transform3D();

    // We can't use a boolean flag since it is possible
    // that after alpha change, this procedure only run
    // once at alpha.finish(). So the best way is to
    // detect alpha value change.
    private float prevAlphaValue = Float.NaN;
    private final WakeupCriterion passiveWakeupCriterion =
        (WakeupCriterion) new WakeupOnElapsedFrames(0, true);

    /**
     * Constructs a RotationSwitchInterpolator.
     *
     * @param target The transformGroup node affected by this transformInterpolator
     */
    public RotationSwitchInterpolator(TransformGroup target) {
        super(null, target);
        this.setEnable(false);
    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.RotationInterpolator#computeTransform(float, org.scijava.java3d.Transform3D)
     */
    @Override
    public void computeTransform(float alphaValue, Transform3D transform) {
        double val = alphaValue * this.getMaximumAngle();

        // construct a Transform3D from:  self * axis * rotation * axisInverse
        _angleTransform.rotY(val);
        transform.setIdentity();
        transform.mul(_currentTransform);
        transform.mul(axis);
        transform.mul(_angleTransform);
        transform.mul(axisInverse);

    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.TransformInterpolator#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration criteria) {
        // Handle stimulus
        WakeupCriterion criterion = passiveWakeupCriterion;
        Alpha alpha = this.getAlpha();

        if (alpha != null) {
            float value = alpha.value();
            if (value != prevAlphaValue) {
                computeTransform(value, _rotationTransform);
                target.setTransform(_rotationTransform);
                prevAlphaValue = value;
            }
            if (!alpha.finished() && !alpha.isPaused()) {
                criterion = defaultWakeupCriterion;
            }
        }
        wakeupOn(criterion);
        if (this.getAlpha() != null && this.getAlpha().finished()) {
            this.stop();
        }
    }

    /**
     * Stops the animation
     */
    public void stop() {
        this.setEnable(false);
        this.setAlpha(null);
    }

    /**
     * Sets the rotation data and start the interpolator.
     *
     * @param alpha            The alpha which gives the duration
     * @param maximumAngle     the ending angle in radians
     * @param transformAxis    the transform that defines the local coordinate system in which this interpolator
     *                         operates. The rotation is done about the Y-axis of this local coordinate system.
     * @param currentTransform the transform that contains the current orientation data of the object.
     */
    public void start(VisualizationClockAlpha alpha, float maximumAngle,
                      Transform3D transformAxis, Transform3D currentTransform) {
        _currentTransform = currentTransform;
        this.setAlpha(alpha);
        this.setMinimumAngle(0f);
        this.setMaximumAngle(maximumAngle);
        this.setTransformAxis(transformAxis);
        this.setEnable(true);
    }
}
