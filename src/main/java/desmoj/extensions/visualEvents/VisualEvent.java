package desmoj.extensions.visualEvents;

import java.util.EventObject;

import desmoj.core.simulator.TimeInstant;

/**
 * This is the basic class for all the events which indicate the changing of the spatial attributes.
 *
 * @author Fred Sun
 */
public abstract class VisualEvent extends EventObject {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -2084982183890572157L;

    /**
     * The SimTime when the event occurred.
     */
    private final TimeInstant _occurredTime;

    /**
     * The name of the target of the spatial operation.
     */
    private final String _affectedSpatialObjectName;

    /**
     * Constructs a VisualEvent.
     *
     * @param source                    The object on which the event initially occurred.
     * @param affectedSpatialObjectName The name of the SpatialObject which is affected by this VisualEvent.
     * @param occuredTime               The time when this event occurred.
     */
    public VisualEvent(Object source, String affectedSpatialObjectName, TimeInstant occurredTime) {
        super(source);
        assert (affectedSpatialObjectName != null) : "It must be specified whom this event is addresed to.";
        _affectedSpatialObjectName = affectedSpatialObjectName;
        _occurredTime = occurredTime;
    }

    /**
     * Gets the SimTime when this event occurred.
     *
     * @return The event occurred time in TimeInstant.
     */
    public TimeInstant getOccurredTime() {
        return _occurredTime;
    }

    /**
     * Gets the ID of the object which is affected by this event.
     *
     * @return the name of the performer.
     */
    public String getTargetName() {
        return _affectedSpatialObjectName;
    }

}
