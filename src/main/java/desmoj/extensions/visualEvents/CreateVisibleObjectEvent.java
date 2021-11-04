package desmoj.extensions.visualEvents;

import desmoj.core.simulator.TimeInstant;

/**
 * This event occurs when a SpatialObject (or MovableSpatialObject) is created.
 *
 * @author Fred Sun
 */
public class CreateVisibleObjectEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the serializable interface.
     */
    private static final long serialVersionUID = 8984003468218479255L;

    //the visual model of the SpatialObject to be created.
    private final String _vModel;

    //indicates whether the created object is movable
    private final boolean _movable;

    /**
     * Constructs a CreateVisibleObjectEvent.
     *
     * @param source       The object on which the event initially occurred.
     * @param objectName   The object which is created.
     * @param type         The type of the SpatialObject to be created.
     * @param movable      Indicates whether the SpatialObject is a MovableSpatialObject or not.
     * @param occurredTime The TimeInstant when the event occurred.
     */
    public CreateVisibleObjectEvent(Object source, String objectName,
                                    String type, boolean movable, TimeInstant occurredTime) {
        super(source, objectName, occurredTime);
        _vModel = type;
        _movable = movable;
    }

    /**
     * Gets the type of the SpatialObject to be created.
     *
     * @return The type of the SpatialObject.
     */
    public String getVisualModel() {
        return _vModel;
    }

    /**
     * Returns true if the SpatialObject to be created is a MovableSpatialObject. Else false.
     *
     * @return True if the Object is a MovableSpatialObject. Else, false.
     */
    public boolean isMovable() {
        return _movable;
    }

}
