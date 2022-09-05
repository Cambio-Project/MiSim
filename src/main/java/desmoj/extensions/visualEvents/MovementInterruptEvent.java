package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * A MovementInterruptEvent indicates that a MovabelSpatialSimProcess's movement has been interrupted.
 *
 * @author Fred Sun
 */
public class MovementInterruptEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = 3886680605789147454L;

    //The position where the involved object has to be when this event occurs.
    private final double[] _stoppedAtPosition;

    /**
     * Constructs a MovementInterruptEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the object who's movement is interrupted.
     * @param stoppedAtPosition         The position where the performer has to be when this event occurs.
     * @param occurredTime              The TimeInstant when the event occurred.
     */
    public MovementInterruptEvent(Object source, String affectedSpatialObjectName,
                                  double[] stoppedAtPosition, TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
        _stoppedAtPosition = stoppedAtPosition;
    }

    /**
     * Returns the position where the involved object has to be when this event occurs.
     *
     * @return The position.
     */
    public double[] getStoppedAtPosition() {
        return _stoppedAtPosition;
    }


}
