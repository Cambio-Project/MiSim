package desmoj.extensions.space3D;

/**
 * The Rotation class specifies a rotation with its axis and angle.
 *
 * @author Fred Sun
 */
public class Rotation {

    /**
     * The angle of the rotation in radians.
     */
    private final double _angle;

    /**
     * A boolean array to indicate which about which axis the rotation should be. The first element is for the X-axis,
     * the second for the Y-axis and the last for the Z-axis.
     */
    private final boolean[] _axis;

    /**
     * The duration of this rotation.
     */
    private final double _duration;

    /**
     * Constructs a Rotation object.
     *
     * @param axis     A 3 elements boolean array to indicate which about which axis the rotation should be. The
     *                 elements refer to the axis X,Y and Z. The combination of more than one axis isn't supported.
     * @param angle    The angle of the rotation in radians.
     * @param duration The duration of this rotation.
     */
    public Rotation(boolean[] axis, double angle, double duration) {
        assert (axis.length == 3) : "The axis argument should be a boolean array with 3 elements.";
        assert (axis[0] == false && axis[1] == false && axis[2] == false) :
            "The axis for the rotation should be specified.";

        _axis = axis;
        _angle = angle;
        _duration = duration;
    }

    /**
     * Gets the angle of the rotation.
     *
     * @return The angle of the rotation in radians.
     */
    public double getAngle() {
        return _angle;
    }

    /**
     * Gets the axis the rotation is about.
     *
     * @return A 3 elements boolean array which associate to the X,Y and Z axis the rotation is about. True stands for
     *     the rotation about this axis.
     */
    public boolean[] getAxis() {
        return _axis;
    }

    /**
     * Gets the duration of this rotation.
     *
     * @return the duration in double.
     */
    public double getDuration() {
        return _duration;
    }

}
