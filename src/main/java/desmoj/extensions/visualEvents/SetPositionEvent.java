package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * This event indicates the setting of a new position for a SpatialObject.
 *
 * @author Fred Sun
 */
public class SetPositionEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -4557866078598069923L;

    /**
     * The new x value.
     */
    private final double _x;

    /**
     * The new y value.
     */
    private final double _y;

    /**
     * The new z value.
     */
    private final double _z;

    /**
     * Constructs a new SetPositionEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the object who's position should be set.
     * @param x                         The new x value.
     * @param y                         The new y value.
     * @param z                         The new z value.
     * @param occuredTime               The TimeInstant when the event occurred.
     */
    public SetPositionEvent(Object source, String affectedSpatialObjectName,
                            double x, double y, double z, TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
        assert (affectedSpatialObjectName != null) : "It must be specified who's position should be reset.";

        _x = x;
        _y = y;
        _z = z;
    }

    /**
     * Gets the new x value.
     *
     * @return The new x value.
     */
    public double getX() {
        return _x;
    }

    /**
     * Gets the new y value.
     *
     * @return The new y value.
     */
    public double getY() {
        return _y;
    }

    /**
     * Gets the new z value.
     *
     * @return The new z value.
     */
    public double getZ() {
        return _z;
    }

}
