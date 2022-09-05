package desmoj.extensions.visualization3d;

import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;
import org.scijava.java3d.TransformInterpolator;
import org.scijava.vecmath.Point3d;
import org.scijava.vecmath.Point3f;
import org.scijava.vecmath.Vector3d;
import org.scijava.vecmath.Vector3f;
import java.util.Enumeration;

/**
 * An interpolator which realize the 3-phase-translation. The translation is divided into 3 phases:</br> - The
 * acceleration phase</br> - The maximum speed phase</br> - The deceleration phase</br> The translation behavior is
 * defined over the speed and duration of each phase.
 *
 * @author Fred Sun
 */
public class MovementPathInterpolator extends TransformInterpolator {

    //the acceleration value of the acceleration phase
    private float _acc;

    //the deceleration value of the deceleration phase
    private float _dec;

    //the alpha value at the and of the acceleration phase
    private float _alphaEndAcc;

    //the alpha value at the beginning of the deceleration phase
    private float _alphaBeginnDec;

    //the translation length of the acceleration phase
    private float _lengthEndAcc;

    //the translation length from the start to the beginning of the
    //deceleration phase
    private float _lengthBeginnDec;

    //the length between each position knot and the start position
    private float[] _lengthRecord;

    //the positions which describes the route of the translation
    private Point3f[] _positionKnots;

    //the speed in each phase
    private float[] _speedKnots;

    //the total duration of the translation
    private float _totalDuration;

    private final TransformGroup _orientationTransform;
    private boolean _focusOnTrack;
    private Vector3f _frontSideVector = null;
    private final Transform3D _rotTransform = new Transform3D();


    /**
     * Creates a MovementPathInterpolator.
     *
     * @param target            The transformGroup node affected by this transformInterpolator
     * @param orientationTarget The TransformGroup responsible for the orientation.
     */
    public MovementPathInterpolator(TransformGroup target,
                                    TransformGroup orientationTarget) {
        super();
        this.setTarget(target);
        _orientationTransform = orientationTarget;
        this.setEnable(false);
    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.TransformInterpolator#computeTransform(float, org.scijava.java3d.Transform3D)
     */
    @Override
    public void computeTransform(float alphaValue, Transform3D transform) {
        float movedLength;

        //get the moved length
        if (alphaValue < _alphaEndAcc) {
            float duration = alphaValue * _totalDuration;
            movedLength = this.getDistanceOfTimedAcceleration(duration, _speedKnots[0], _acc);
        } else if (alphaValue < _alphaBeginnDec) {
            float durationThisPhase = (alphaValue - _alphaEndAcc) * _totalDuration;
            movedLength = _lengthEndAcc + (durationThisPhase * _speedKnots[1]);
        } else {
            float durationThisPhase = (alphaValue - _alphaBeginnDec) * _totalDuration;
            movedLength =
                _lengthBeginnDec + this.getDistanceOfTimedAcceleration(durationThisPhase, _speedKnots[1], _dec);
        }

        //calculate the position according to the moved length and
        //set the position.
        transform.setTranslation(this.getCurrentPositionAndUpdateDirection(movedLength, transform));
    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.TransformInterpolator#processStimulus(java.util.Enumeration)
     */
    @Override
    public void processStimulus(Enumeration criteria) {
        super.processStimulus(criteria);
        if (this.getAlpha() != null && this.getAlpha().finished()) {
            this.stop();
        }
    }

    /**
     * Stops the animation.
     */
    public void stop() {
        this.setEnable(false);
        this.setAlpha(null);
    }


    /**
     * Gets the acceleration value based on the initial, the end speed and the acceleration duration.
     *
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @param duration     The duration of the acceleration.
     * @return The acceleration.
     */
    private float getAcceleration(float initialSpeed, float endSpeed, float duration) {
        if (duration == 0) {
            return 0f;
        } else {
            return (endSpeed - initialSpeed) / duration;
        }
    }

    /**
     * Updates the position based of the moved length. The directional data will also be updates if focusOnTrack is
     * set.
     *
     * @param movedLength The moved length from the start of the translation
     * @param transform   The target's positional transform
     */
    private Vector3f getCurrentPositionAndUpdateDirection(float movedLength,
                                                          Transform3D transform) {
        int numPoints = _lengthRecord.length;
        Vector3f movedVec = null;
        for (int i = 1; i < numPoints; i++) {
            if (_lengthRecord[i] >= movedLength) {
                //the vector with full length
                movedVec = new Vector3f(_positionKnots[i].x - _positionKnots[i - 1].x,
                    _positionKnots[i].y - _positionKnots[i - 1].y,
                    _positionKnots[i].z - _positionKnots[i - 1].z);

                if (_focusOnTrack) {
                    this.updateDirection(new Point3d(movedVec.x, movedVec.y, movedVec.z));
                }

                //the length ratio in the section
                float lengthRatio = (movedLength - _lengthRecord[i - 1]) / (_lengthRecord[i] - _lengthRecord[i - 1]);

                //adjust the length of the vector
                movedVec.x *= lengthRatio;
                movedVec.y *= lengthRatio;
                movedVec.z *= lengthRatio;

                //add the start position
                movedVec.add(_positionKnots[i - 1]);

                return movedVec;
            }
        }
        return new Vector3f(_positionKnots[_positionKnots.length - 1]);
    }

    private void updateDirection(Point3d moveVec) {
        _rotTransform.lookAt(new Point3d(), moveVec, new Vector3d(0, 1, 0));
        _rotTransform.invert();
        _orientationTransform.setTransform(_rotTransform);
    }

    /**
     * Gets the distance of the acceleration or deceleration from an initial speed to an end speed with a given
     * acceleration or deceleration value.
     *
     * @param initialSpeed The initial speed.
     * @param endSpeed     The end speed.
     * @param acc          The acceleration or deceleration.
     * @return The distance needed.
     */
    private float getDistanceOfSpeedBoundedAcceleration(float initialSpeed, float endSpeed, float acc) {
        if (acc == 0) {
            return 0f;
        } else {
            return (endSpeed * endSpeed - initialSpeed * initialSpeed) / (2 * acc);
        }
    }

    /**
     * Gets the distance moved based on the acceleration and the duration.
     *
     * @param duration     The duration of the movement.
     * @param initialSpeed The speed offset.
     * @param acc          The acceleration.
     * @return The distance moved with the specified duration after the duration.
     */
    private float getDistanceOfTimedAcceleration(float duration, float initialSpeed, float acc) {
        if (acc >= 0) {
            return (acc * duration * 0.5f * duration) + (initialSpeed * duration);
        } else {
            return Math.max(0.0f, (acc * duration * 0.5f * duration) + (initialSpeed * duration));
        }
    }


    /**
     * Calculates the length between each position knot and the start position. The result will be stored in the length
     * record class variable.
     */
    private void handleLengthRecord() {
        assert (_positionKnots != null) : "The parameter can't be null.";
        assert (_positionKnots.length > 1) : "There must be more than one point to" +
            " calculate the distance between them.";

        //the first element is the length from the start to the start.
        _lengthRecord = new float[_positionKnots.length];
        _lengthRecord[0] = 0f;
        for (int i = 1; i < _positionKnots.length; i++) {
            _lengthRecord[i] = _lengthRecord[i - 1] +
                _positionKnots[i].distance(_positionKnots[i - 1]);
        }
    }

    /**
     * Sets the movement data and start the interpolator.
     *
     * @param alpha         The alpha which gives the duration
     * @param durationKnots A float[3] array which describes the duration of the 3 phases movement.
     * @param speedKnots    A float[3] array which describes the speed of the 3 phases movement.
     * @param positionKnots An array of points which describe the route of the movement.
     */
    public void start(VisualizationClockAlpha alpha,
                      float[] durationKnots, float[] speedKnots, Point3f[] positionKnots) {

        //sets the data
        this.setData(alpha, durationKnots, speedKnots, positionKnots);

        //prepare the length variables
        this.handleLengthRecord();
        _focusOnTrack = false;
        this.setEnable(true);
    }

    public void startWithFocusOnTrack(VisualizationClockAlpha alpha,
                                      float[] durationKnots, float[] speedKnots, Point3f[] positionKnots,
                                      Vector3d frontSideVec) {
        this.setData(alpha, durationKnots, speedKnots, positionKnots);
        this.handleLengthRecord();
        _focusOnTrack = true;
        _frontSideVector = new Vector3f((float) frontSideVec.x, (float) frontSideVec.y, (float) frontSideVec.z);
        this.setEnable(true);
    }

    private void setData(VisualizationClockAlpha alpha,
                         float[] durationKnots, float[] speedKnots, Point3f[] positionKnots) {
        assert (alpha != null) : "The alpha must be specified.";
        assert (durationKnots.length == 3) : "The number of the duration knots must be " +
            "3 to describe the 3 phase movement.";
        assert (speedKnots.length == 3) : "The number of the speed knots must be " +
            "3 to describe the 3 phase movement.";
        assert (positionKnots != null) : "The position knots knots can't be null.";
        assert (speedKnots != null) : "The speed must be specified in each phase.";
        if (positionKnots.length < 2) {
            throw new IllegalArgumentException("A translation needs at least " +
                "two points to define it.");
        }

        this.setAlpha(alpha);

        //save the positions
        _positionKnots = positionKnots;
        _speedKnots = speedKnots;
        _totalDuration = 0f;

        //calculate the alpha values associated to the duration and speed
        //knots
        for (int i = 0; i < 3; i++) {
            _totalDuration += durationKnots[i];
        }
        _alphaEndAcc = durationKnots[0] / _totalDuration;
        _alphaBeginnDec = (durationKnots[1] / _totalDuration) + _alphaEndAcc;

        //calculate the acc and dec of this movement
        _acc = this.getAcceleration(speedKnots[0], speedKnots[1], durationKnots[0]);
        _dec = this.getAcceleration(speedKnots[1], speedKnots[2], durationKnots[2]);

        //calculate the length knots associated to the alpha knots
        _lengthEndAcc = this.getDistanceOfSpeedBoundedAcceleration(speedKnots[0], speedKnots[1], _acc);
        _lengthBeginnDec = (speedKnots[1] * durationKnots[1]) + _lengthEndAcc;
    }
}
