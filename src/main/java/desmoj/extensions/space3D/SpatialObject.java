package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;
import java.util.Set;


/**
 * The basic interface of the SpatialObject. It contains simple setting and getting methods of coordinates for every
 * visible object in the simulation.
 * </br></br>
 * Every SpatialObject may contain several or none entry or exit point. They define the specific coordinates where an
 * MovableSpatialObject move to or get out, if they navigate into or out of this SpatialObject.
 *
 * @author Fred Sun
 */
public interface SpatialObject {

    //	/**
    //	 * Gets a 4x4 matrix back which represents the coordinates of the SpatialObject.
    //	 * @return The double[4][4] where the result will be stored.
    //	 */
    //	public abstract double[][] getMatrix();


    /**
     * Adds an entry point to this SpatialObject. The given position should refer to the local coordinates of this
     * SpatialObject.
     *
     * @param name The name of this entry point.
     * @param xPos The x-position of this entry point in local coordinates.
     * @param yPos The y-position of this entry point in local coordinates.
     * @param zPos The z-position of this entry point in local coordinates.
     */
    void addEntryPoint(String name, Length xPos, Length yPos, Length zPos);

    /**
     * Adds an exit point to this SpatialObject. The given position should refer to the local coordinates of this
     * SpatialObject.
     *
     * @param name The name of this exit point.
     * @param xPos The x-position of this exit point in local coordinates.
     * @param yPos The y-position of this exit point in local coordinates.
     * @param zPos The z-position of this exit point in local coordinates.
     */
    void addExitPoint(String name, Length xPos, Length yPos, Length zPos);

    /**
     * Gets a set of names of all the entry points this SpatialObject contains.
     *
     * @return A set of names of the entry points.
     */
    Set<String> getEntryPointNames();

    /**
     * Gets a set of names of all the exit points this SpatialObject contains.
     *
     * @return A set of names of the exit points.
     */
    Set<String> getExitPointNames();

    /**
     * Gets the position of the given entry point.
     *
     * @param name The name of the entry point.
     * @return The position of the entry point in local coordinates. The first element of the array represents the
     *     x-position, the second the y-position and the third the z-position. It returns null if the SpatialObject
     *     doesn't contain the entry point with the given name.
     */
    Length[] getEntryPoint(String name);

    /**
     * Gets the position of the entry point in global coordinates.
     *
     * @param name The name of the entry point.
     * @return The position of the entry point in global coordinates. The first element of the array represents the
     *     x-position, the second the y-position and the third the z-position. It returns null if the SpatialObject
     *     doesn't contain the entry point with the given name.
     */
    Length[] getEntryPointGlobal(String name);

    /**
     * Gets the position of the given exit point.
     *
     * @param name The name of the exit point.
     * @return The position of the exit point in local coordinates. The first element of the array represents the
     *     x-position, the second the y-position and the third the z-position. It returns null if the SpatialObject
     *     doesn't contain the exit point with the given name.
     */
    Length[] getExitPoint(String name);

    /**
     * Gets the position of the exit point in global coordinates.
     *
     * @param name The name of the exit point.
     * @return The position of the exit point in global coordinates. The first element of the array represents the
     *     x-position, the second the y-position and the third the z-position. It returns null if the SpatialObject
     *     doesn't contain the exit point with the given name.
     */
    Length[] getExitPointGlobal(String name);

    /**
     * Gets a 4x4 double matrix object which contains the spatial data of the SpatialObejct.
     *
     * @return The Matrix4d object.
     */
    Matrix4d getMatrix();

    /**
     * Returns the name of the named object. This is the same name displayed in reports and trace files when this named
     * object is shown in those reports or trace files.
     *
     * @return The name of the named object.
     */
    String getName();

    /**
     * @return It returns the x-position of the object.
     */
    Length getPosX();

    /**
     * @return It returns the y-position of the object.
     */
    Length getPosY();

    /**
     * @return It returns the z-position of the object.
     */
    Length getPosZ();

    /**
     * Check whether this Object is a MovableSpatialObject or not.
     *
     * @return Return true if this SpatialObject is also a MovableSpatialObject. Else, false.
     */
    boolean isMovable();

    /**
     * Resets the rotation of the object.
     */
    void resetRotation();

    /**
     * Adds a rotation about the x-axis to the current orientation.
     *
     * @param angle The rotation angle in radians
     */
    void rotX(double angle);

    /**
     * Adds a rotation about the y-axis to the current orientation.
     *
     * @param angle The rotation angle in radians
     */
    void rotY(double angle);

    /**
     * Adds a rotation about the z-axis to the current orientation.
     *
     * @param angle The rotation angle in radians
     */
    void rotZ(double angle);


    //	/**
    //	 * Update the current spatial data of the object.
    //	 * This method should only be used for the internal coordination, not for the position/orientation manipulation.
    //	 * The method will change the 3D properties of the SpatialObject. But the visualization instance, if used,
    //	 * won't be informed about the change. Therefore the visualization won't be updated. Inconsistency could
    //	 * be the result.
    //	 * @param matrix The new matrix in a double[16] for the object. The first 4 elements are the first row, the next 4 are the second row etc.
    //	 */
    //	public abstract void update(double[] matrix);

    //	/**
    //	 * Update the current spatial data of the object.
    //	 * This method should only be used for the internal coordination, not for the position/orientation manipulation.
    //	 * The method will change the 3D properties of the SpatialObject. But the visualization instance, if used,
    //	 * won't be informed about the change. Therefore the visualization won't be updated. Inconsistency could
    //	 * be the result.
    //	 * @param matrix The new matrix in Matrix4d.
    //	 */
    //	public abstract void update(Matrix4d matrix);

    /**
     * It sets the new position for the object.	 *
     *
     * @param x The ExtendedLength which contains the new x-position.
     * @param y The ExtendedLength which contains the new y-position.
     * @param z The ExtendedLength which contains the new z-position.
     */
    void setPosition(Length x, Length y, Length z);

}
