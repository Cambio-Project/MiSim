package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * The AttachEvent indicates that a MovableSpatialObject is attached to an other.
 *
 * @author Fred Sun
 */
public class AttachEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -2562931478132749376L;

    //The host to attached to.
    private final String _hostName;

    /**
     * Constructs an AttachEvent.
     *
     * @param source             The object on which the event initially occurred.
     * @param attacherObjectName The name of the object who's attached to a host.
     * @param The                name of the object which is attached by the attacher.
     * @param occurredTime       The TimeInstant when the event occurred.
     */
    public AttachEvent(Object source, String attacherObjectName,
                       String hostName, TimeInstant occurredTime) {
        super(source, attacherObjectName, occurredTime);
        assert (hostName != null) : "It must be specified which MovableSpatialObject is attached to.";
        _hostName = hostName;
    }

    /**
     * Gets the ID of the host.
     *
     * @return The ID of the host.
     */
    public String getHostName() {
        return _hostName;
    }

}
