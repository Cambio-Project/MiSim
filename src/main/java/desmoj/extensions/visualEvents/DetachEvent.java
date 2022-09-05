package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * The AttachEvent indicates that a MovableSpatialObject is detached.
 *
 * @author Fred Sun
 */
public class DetachEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = 921321458109950401L;

    /**
     * Constructs a DetachEvent.
     *
     * @param source           The object on which the event initially occurred.
     * @param detachObjectName The name of the object who's detached from a host.
     * @param occurredTime     The TimeInstant when the event occurred.
     */
    public DetachEvent(Object source, String detachObjectName,
                       TimeInstant occurredTime) {
        super(source, detachObjectName, occurredTime);
    }

}
