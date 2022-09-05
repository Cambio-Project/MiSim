package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.space3D.Rotation;

/**
 * The RotationEvent indicates the event when a SpatialObject is be rotated or a new angle is set. It could be received
 * by visualizing instances or animation recording instances.
 *
 * @author Fred Sun
 */
public class RotateEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -7130382830972567789L;

    /**
     * The rotation which should be performed.
     */
    private final Rotation _rotation;

    /**
     * Constructs a RotationEvent.
     *
     * @param source       The object on which the event initially occurred.
     * @param rotaterName  The name of the object who's rotating.
     * @param rotation     The Rotation object which represents the rotation which should be performed.
     * @param duration     The duration of the rotation. The value zero is allowed and indicates that the rotation will
     *                     be set immediately without an animation.
     * @param occurredTime The TimeInstant when the event occurred.
     */
    public RotateEvent(Object source, String rotaterName, Rotation rotation,
                       TimeInstant occurredTime) {
        super(source, rotaterName, occurredTime);
        assert (rotaterName != null) : "It must be specified who should perform the rotation.";
        assert (rotation != null) : "The rotation must be specified.";

        _rotation = rotation;
    }

    /**
     * Gets the Rotation object.
     *
     * @return A Rotation object which represents the rotation which should be performed.
     */
    public Rotation getRotation() {
        return _rotation;
    }

}
