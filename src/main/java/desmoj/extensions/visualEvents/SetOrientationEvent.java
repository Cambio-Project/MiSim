package desmoj.extensions.visualEvents;

import org.scijava.vecmath.Quat4d;

import desmoj.core.simulator.TimeInstant;

/**
 * This is an event which signals the reset of the rotation by an object.
 *
 * @author Fred Sun
 */
public class SetOrientationEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -3166991489773406464L;

    //The orientation to be set.
    private final Quat4d _orientation;

    /**
     * Constructs a SetOrientationEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the object who's orientation should be set.
     * @param orientation               The orientation should be set.
     * @param occurredTime              The TimeInstant when the event occurred.
     */
    public SetOrientationEvent(Object source, String affectedSpatialObjectName,
                               Quat4d orientation, TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
        assert orientation != null : "The new orientation must be specified.";
        _orientation = orientation;
    }

    /**
     * Gets the orientation to be set by this event.
     *
     * @return The orientation to be set.
     */
    public Quat4d getOrientation() {
        return _orientation;
    }
}
