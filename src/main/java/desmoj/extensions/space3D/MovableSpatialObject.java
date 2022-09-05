package desmoj.extensions.space3D;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.TimeSpan;


/**
 * This interface extends the SpatialObject interface by moving animation methods. The class which implements this is
 * meant to be a movable object in the simulation. The methods cover the setting and getting spatial transformation
 * data.
 * <p>
 * A MovableSpatialObject (MSO) can contain other MovableSpatialObjects as components to simulate the movable
 * extensions/parts of an object. The other way around, a MSO can also be the child-MSO of other MSOs. In this case, the
 * Attach and Detach methods will be deactivated. All the getting methods will return the local attributes without
 * considering the parent MSO. The position and the orientation are represented in the parent-MSO's local coordinates.
 * For the move methods only the move(movement, focusOnTrack) will be remained usable. To declare a MSO as a child-MSO,
 * the parent MSO must be give in the constructor. This is the only way to declare it and it can't be modified
 * afterwards.
 *
 * @author Fred Sun
 */
public interface MovableSpatialObject extends SpatialObject {

    //	/**
    //	 * Add a child-component to this MovableSpatialObject. This Method shouldn't
    //	 * be called by user. The arrangement will be done by the specific
    //	 * constructor.
    //	 * @param childMSO The child-MovableSpatialObject to be added.
    //	 */
    //	public abstract void addChildMSO(MovableSpatialObject childMSO);

    /**
     * Snaps to another MovableSpatialObject. If the host moves, this object will be moved, too. The relative position
     * to the host object will be kept. If attached, this object shouldn't be able to change its position and
     * orientation itself anymore.
     *
     * @param host The MovableSpatialObject who to attach to.
     */
    void attach(MovableSpatialObject host);

    /**
     * Cancel the movement if there is a movement performing.
     */
    void cancelMovement();

    /**
     * Cancel the rotation if there is a rotation performing.
     */
    void cancelRotation();

    //	/**
    //	 * Returns true if this object contains the given MovableSpatialObject as child-component.
    //	 * @param childMSO The MovableSpatialObject to be checked.
    //	 * @return True if this object contains it as childMSO, false if not.
    //	 */
    //	public abstract boolean containsChildMSO(MovableSpatialObject childMSO);

    /**
     * Releases from the host object if this object is attached to it.
     */
    void detach();

    /**
     * Gets the acceleration of the object. The default value is 0.
     *
     * @return The acceleration
     */
    double getAcc();

    //	/**
    //	 * Gets an iterator over all the child-MovableSpatialObejct's this object contains
    //	 * @return An iterator over all the child-MSO's this object contains.
    //	 */
    //	public abstract Iterator<MovableSpatialObject> getAllChildMSO();

    /**
     * Sets the acceleration of the object.
     *
     * @param acc The acceleration
     */
    void setAcc(double acc);

    /**
     * Returns the SpatialObject where this MovableSpatialObject is currently located.
     *
     * @return The SpatialObject where this MovableSpatialObject is located. Null, if it isn't located at any
     *     SpatialObject.
     */
    SpatialObject getCurrentLocation();

    /**
     * Gets the current Movement object.
     *
     * @return The current Movement object. Null, if this MovableSpatialObject isn't moving.
     */
    Movement getCurrentMovement();

    /**
     * Gets the current Rotation object.
     *
     * @return The current Rotation object. Null, if this MovableSpatialObject isn't rotating.
     */
    Rotation getCurrentRotation();

    /**
     * Gets the current moving speed of the object.
     *
     * @return The current moving speed.
     */
    double getCurrentSpeed();

    //	/**
    //	 * Gets the vector which points to the front side of this MovableSpatialObject.
    //	 * The default vector is null.
    //	 * @return The normalized vector which shows the front side.
    //	 */
    //	public abstract Vector3d getFrontSideVector();

    /**
     * Gets the negative acceleration of the object. The default value is 0.
     *
     * @return The negative acceleration
     */
    double getDec();

    //	/**
    //	 * Gets how many child-MovableSpatialObject's this object has.
    //	 * @return The number of the child-MSO's of this obejct.
    //	 */
    //	public abstract int getNumberOfChildMSO();

    //	/**
    //	 * Gets the parent-MovableSpaitalObject if this Object is a component of it
    //	 * @return The parent-MSO if exist. Else, null.
    //	 */
    //	public abstract MovableSpatialObject getParentMSO();

    //	/**
    //	 * Check whether this object has any child-MovableSpatialObject
    //	 * @return True if there is a childMSO. False if there is no childMSO.
    //	 */
    //	public abstract boolean hasChildMSO();

    /**
     * Sets the negative acceleration of the object.
     *
     * @param dec The negative acceleration
     */
    void setDec(double dec);

    /**
     * Gets the maximum speed of the object. If it's 0, the MovableSpatialObject won't have speed limitation. The
     * default value is 0.
     *
     * @return The maximum speed of the object
     */
    double getMaxSpeed();

    /**
     * Sets the maximum speed of this object
     *
     * @param maxSpeed The maximum speed of the object
     */
    void setMaxSpeed(double maxSpeed);

    /**
     * IShows whether the MovableSpatialObject is attached.
     *
     * @return Whether the MovableSpatialObejct is attached.
     */
    boolean isAttached();

    //	/**
    //	 * Show whether the MovableSpatialObject is a child-component of another MovableSpatialObject
    //	 * @return true, if this MovableSpatialObject is part of another MovableSpatialObject. False, if it's not.
    //	 */
    //	public abstract boolean isChildMSO();

    /**
     * Returns whether the FocusOnTrack function is on.
     *
     * @return True if it's set. False if it's not set.
     */
    boolean isFocusedOnTrack();

    /**
     * Shows whether the MovableSpatialObject is moving.
     *
     * @return whether the MovableSpatialObject is moving.
     */
    boolean isMoving();

    /**
     * Shows whether the MovableSpatialObject is rotating.
     *
     * @return whether the MovableSpatialObject is rotating.
     */
    boolean isRotating();

    /**
     * Move the object to the position of another SpatialObject without a specified duration. It use the acceleration,
     * deceleration and the maximum speed of the MovableSpatialObject. For this method the maximum speed of the object
     * must be given or an IllegalMoveException will be thrown. If the acceleration is <= 0 OR deceleration of the
     * object is >= 0, then the kinematical attributes won't be calculated and the movement will start with the maximum
     * speed and break to zero immediately at the destination. If the object is already moving it will move to the new
     * destination. (not for MovableSpatialSimProcess)
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    void move(SpatialObject destination, String entryPointName, boolean focusOnTrack)
        throws SuspendExecution;

    /**
     * Move the object to the position of another SpatialObject with a specified duration. It use the acceleration,
     * deceleration and the maximum speed of the MovableSpatialObject. If the acceleration is <= 0 OR deceleration of
     * the object is >= 0, then the kinematical attributes won't be calculated and the movement will start with the
     * speed needed to arrive the destination in time. If the maximum speed of the object is initialized (>0), then an
     * IllegalMoveException will be thrown if it is not possible to make to the destination in time. If the object is
     * already moving it will move to the new destination. (not for MovableSpatialSimProcess)
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param duration        The duration of the movement in TimeSpan.
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    void move(SpatialObject destination, String entryPointName, TimeSpan duration, boolean focusOnTrack)
        throws SuspendExecution;

    /**
     * Move the object to the position of another SpatialObject with specific attributes. There are different varieties
     * of movement modes depends on the initialized parameters (O = initialized, X = not initialized(means invalid or
     * null)):
     * <p>
     * case 1: duration O, maxSpeed O, acc & dec O: The movement will be performed according to the parameters. If the
     * given parameters can't be satisfied, an IllegalMoveException will be thrown.
     * <p>
     * case 2: duration X, maxSpeed O, acc & dec O: The object will be moved to the destination with the fastest speed
     * possible.
     * <p>
     * case 3: duration O, maxSpeed X, acc & dec O: The object will move to the destination with the given acceleration
     * and deceleration and try to reach the destination in time. If the given parameters can't be satisfied, an
     * IllegalMoveException will be thrown.
     * <p>
     * case 4: duration X, maxSpeed X, acc & dec O: The object will be accelerated and followed by a deceleration.
     * There's no constant phase in the movement.
     * <p>
     * case 5: duration O, maxSpeed O, acc or dec X: The object will start with the speed needed to arrive the
     * destination in the given time and break suddenly to zero at the end of the movement. The maxSpeed gives the speed
     * limit of the movement. If the given parameters can't be satisfied, an IllegalMoveException will be thrown.
     * <p>
     * case 6: duration X, maxSpeed O, acc or dec X: The movement will start with the maxSpeed and stops the
     * destination.
     * <p>
     * case 7: duration O, maxSpeed X, acc or dec X: The movement will start with the speed needed to arrive the
     * destination with the duration given.
     * <p>
     * All other combinations of these parameters will cause an IllegalMoveException.
     * <p>
     * If the object is already moving it will move to the new destination. (not for MovableSpatialSimProcess)
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param duration        The duration of the movement in TimeSpan(optional).
     * @param maxSpeed        The maximum speed of the movement(optional).
     * @param acc             The acceleration of the movement(optional).
     * @param dec             The deceleration of the movement(optional).
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    void move(SpatialObject destination, String entryPointName, TimeSpan duration, double maxSpeed,
              double acc, double dec, boolean focusOnTrack) throws SuspendExecution;

    /**
     * Move the object to the destination according to the given movement object. If the object is already moving it
     * will move to the new destination. (not for MovableSpatialSimProcess) If this MovableSpatialObject is a child-MSO,
     * the Track which is contained in the Movement parameter should be in the local coordinates of this object.
     *
     * @param movement     The movement which should be performed.
     * @param focusOnTrack Whether the orientation of the moving object should focus to the moving direction.
     */
    void move(Movement movement, boolean focusOnTrack) throws SuspendExecution;

    /**
     * Notifies the MovableSpatialObject that the moving is finished and updates the new coordinates. This method
     * shouldn't be called by user!
     */
    void notifyMovingDone() throws SuspendExecution;

    /**
     * Notifies the MovableSpatialObject that the rotating is finished and updates the new coordinates. This method
     * shouldn't be called by user!
     */
    void notifyRotatingDone() throws SuspendExecution;

    /**
     * Rotates the object about the x-axis using an animation.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    void rotX(double angle, TimeSpan duration) throws SuspendExecution;

    /**
     * Rotates the object about the y-axis using an animation.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    void rotY(double angle, TimeSpan duration) throws SuspendExecution;

    /**
     * Rotates the object about the z-axis  using an animation.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    void rotZ(double angle, TimeSpan duration) throws SuspendExecution;

    /**
     * Set this object to the position of the destination object and change the currentLocation to the destination
     * object.
     *
     * @param destination The destination this object will be send to.
     */
    void sendToLocation(SpatialObject destination);

    /**
     * Set this object to the position of the given entry point of the destination object. The currentLocation will also
     * be changed to the destination object.
     *
     * @param destination The destination object the currentLocation should be set to.
     * @param entryPoint  The specific entry point of the destination object.
     */
    void sendToLocationEntryPoint(SpatialObject destination, String entryPoint);

    /**
     * Set this object to the position of the given exit point of the destination object. The currentLocation will also
     * be changed to the destination object.
     *
     * @param destination The destination object the currentLocation should be set to.
     * @param exitPoint   The specific exit point of the destination object this object will be send to.
     */
    void sendToLocationExitPoint(SpatialObject destination, String exitPoint);

    //	/**
    //	 * Sets the vector which points from the origin to the front side of this MovableSpatialObject.
    //	 * @param frontSide The vector which point points to the front side.
    //	 */
    //	public abstract void setFrontSideVector(Vector3d frontSide);

}