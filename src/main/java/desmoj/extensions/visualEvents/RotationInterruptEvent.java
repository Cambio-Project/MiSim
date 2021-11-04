package desmoj.extensions.visualEvents;

import org.scijava.vecmath.Quat4d;

import desmoj.core.simulator.TimeInstant;

/**
 * A RotationInterruptEvent indicates that a MovabelSpatialSimProcess's rotation has been interrupted.
 *
 * @author Fred Sun
 */
public class RotationInterruptEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = 627853614449149911L;

    //The orientation the performer has to have when this event occurs.
    private final Quat4d _stoppedAtOrientation;

    /**
     * Constructs a RotationInterruptEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the object who's rotation is interrupted.
     * @param stoppedAtOrientation      The orientation the performer has to have when this event occurs.
     * @param occurredTime              The TimeInstant when the event occurred.
     */
    public RotationInterruptEvent(Object source, String affectedSpatialObjectName,
                                  Quat4d stoppedAtOrientation, TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
        assert stoppedAtOrientation != null : "The orientation of the performer must be specified.";
        _stoppedAtOrientation = stoppedAtOrientation;
    }

    /**
     * Returns the orientation the performer has to have when this event occurs.
     *
     * @return The orientation in Quat4d.
     */
    public Quat4d getStoppedAtOrientation() {
        return _stoppedAtOrientation;
    }

}
