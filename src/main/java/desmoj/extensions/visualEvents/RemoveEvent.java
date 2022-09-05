package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * A RemoveEvent indicates that a SpatialObject isn't needed anymore.
 *
 * @author Fred Sun
 */
public class RemoveEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -379040594128850162L;

    /**
     * Constructs a RemoveEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the object which should be removed.
     * @param occurredTime              The TimeInstant when the event occurred.
     */
    public RemoveEvent(Object source, String affectedSpatialObjectName,
                       TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
    }

}
