package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * This event sets the visibility of the visible representation of an object.
 *
 * @author Fred Sun
 */
public class SetVisibleEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -6852259201179877085L;

    private final boolean _visibleEnable;

    /**
     * @param source
     * @param affectedSpatialObjectName
     * @param occurredTime
     */
    public SetVisibleEvent(Object source, String affectedSpatialObjectName,
                           boolean visibleEnable, TimeInstant occurredTime) {
        super(source, affectedSpatialObjectName, occurredTime);
        _visibleEnable = visibleEnable;
    }

    /**
     * Gets the boolean value which shows whether the target should be visible or not.
     *
     * @return True, if the target should be visible. Else, false.
     */
    public boolean getVisible() {
        return _visibleEnable;
    }

}
