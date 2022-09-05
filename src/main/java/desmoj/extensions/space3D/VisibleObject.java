package desmoj.extensions.space3D;

/**
 * This interface defines the methods for objects which should be visualized.
 * </br></br>
 * Every VisibleObject contains a type which indicates the 3D-Model at the visualization.
 *
 * @author Fred Sun
 */
public interface VisibleObject {

    /**
     * Get the type of the SpatialObject.
     *
     * @return The type of the SpatialObject.
     */
    String getVisualModel();

    /**
     * Shows the visibility of the visual representation of this object.
     *
     * @return True is this object is visible. Else, false.
     */
    boolean isVisible();

    /**
     * Sets the visibility of the visual representation of this object.
     *
     * @param visible True, if visible. Else, false.
     */
    void setVisible(boolean visible);

    /**
     * Sends a RemoveEvent to signal the observers that this object isn't needed anymore.
     * </br></br>
     * WARNING: The SpatialObject will be still existing.
     */
    void removeVisible();

}
