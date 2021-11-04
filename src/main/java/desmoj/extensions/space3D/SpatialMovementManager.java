package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;
import org.scijava.vecmath.Quat4d;
import org.scijava.vecmath.Vector3d;
import java.util.concurrent.TimeUnit;

import desmoj.core.report.ErrorMessage;
import desmoj.core.simulator.Entity;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.ModelComponent;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeOperations;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.visualEvents.AttachEvent;
import desmoj.extensions.visualEvents.DetachEvent;
import desmoj.extensions.visualEvents.MoveEvent;
import desmoj.extensions.visualEvents.RotateEvent;
import desmoj.extensions.visualEvents.SetOrientationEvent;
import desmoj.extensions.visualEvents.SetPositionEvent;
import desmoj.extensions.visualEvents.VisualEventTransmitter;

/**
 * A SpatialMovementManager is (only) associated to every movable descendant of MovableSpatialEntity and
 * MovableSpatialSimProcess. It serves the interaction between the SpatialObjects, the IVisualObject, and the simulation
 * model. It gets tracks from the model,calculates the duration, holds it self back during the animation and updates the
 * coordinates to the SpatialObjects.
 *
 * @author Fred Sun
 */
public class SpatialMovementManager {

    //The host which attached to (if attached).
    private MovableSpatialObject _attachHost = null;

    //The associated MovableSpatialObject
    private final MovableSpatialObject _clientObject;

    //stores the current scheduled MovingDoneEvent or RotatingDoneEvent
    //	private Event<Entity> _currentDoneEvent = null;
    private ExternalEvent _currentDoneEvent = null;

    //The current Movement object. If the MovableSpatialObject is not rotating, this object should be null
    private Movement _currentMovement = null;

    //The current Rotation object. If the MovableSpatialObject is not rotating, the Rotation should be null
    private Rotation _currentRotation = null;

    //a buffer for the Moving/RotatingDoneEvent which should be scheduled after the currentDoneEvent has been processed
    //	private Event<Entity> _doneEventBuffer = null;
    private ExternalEvent _doneEventBuffer = null;

    //the simulation time when the buffered Moving/RotatingDoneEvent should be ended.
    private TimeInstant _doneEventEndingTime = null;

    //The singleton for sending the SpatialEvents
    private VisualEventTransmitter _eventTransmitter = null;

    //The SimTime when a movement is started. If the object isn't moving, the movingStartTime should be null.
    private TimeInstant _movingStartTime = null;

    private final SpatialLayoutManager _layoutManager = SpatialLayoutManager.getSpatialLayoutManager();

    //The model this SimProcess is associated to
    private final Model _owner;

    //The offset quaternion to the attached host.
    private Quat4d _orientaionOffsetDuringAttachment = new Quat4d();

    //The SimTime when a rotation is started. If the object isn't rotating, the rotatingStartTime should be null.
    private TimeInstant _rotatingStartTime = null;

    //The offset vector from the host to this object in the local coordinate of the host.
    private Vector3d _positionOffsetDuringAttachment = new Vector3d();

    private Vector3d _frontSideVector = new Vector3d();

    /**
     * It constructs a SpatialMovementManager object associated to the MovableSpatialObject
     *
     * @param simulationModel The model which controls the simulation
     * @param sObject         The associated MovableSpatialObject which should be a descendant of the
     *                        MovableSpatialEntity or MovableSpatialSimProcess
     */
    protected SpatialMovementManager(Model simulationModel, MovableSpatialObject sObject) {

        assert ((sObject instanceof MovableSpatialEntity) || (sObject instanceof MovableSpatialSimProcess)) :
            "A SpatialMovementManager can only be used by a MovableSpatialEntity or MovableSpatialSimProcess. Please check your code.";

        _owner = simulationModel;
        _clientObject = sObject;
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
    }

    /**
     * Schedules and organizes the order of the MovingDone-/RotatingDoneEvent. If both are present, compare them and
     * schedule the one which ends first and save the other one for later.
     *
     * @param evt         The new Moving-/RotatingDoneEvent which wants to be scheduled.
     * @param simDuration The duration when this event should be process since now.
     */
    private void scheduleDoneEvent(ExternalEvent evt, TimeSpan simDuration) {
        //if a Moving-/RotatingDoneEvent isn't scheduled already
        //schedule the current Moving-/RotatingDoneEvent.
        //otherwise compare them. Schedule the one which comes first and remember the other one for later.
        //		if(((Entity)_clientObject).isScheduled()){
        if (_currentDoneEvent != null) {

            //The simulation time when this movement should be ended
            //			SimTime movingEndTime = SimTime.add(simDuration, SimTime.NOW);
            TimeInstant movingEndTime = TimeOperations.add(simDuration, ((ModelComponent) _clientObject).presentTime());

            //if the current action should be ended after the scheduled spatial action
            //buffer the event for current action for later
            if (TimeInstant.isAfter(movingEndTime, _currentDoneEvent.scheduledNext())) {
                _doneEventBuffer = evt;
                _doneEventEndingTime = movingEndTime;
                //else replace the scheduled event with the current Event and save the old scheduled event for later
            } else {
                //save the old event and ending time
                _doneEventBuffer = _currentDoneEvent;
                _doneEventEndingTime = _currentDoneEvent.scheduledNext();

                //remove it from the scheduler
                //				((Entity)_clientObject).cancel();
                _currentDoneEvent.cancel();

                //schedule the new Event
                _currentDoneEvent = evt;
                //				((Entity)_clientObject).schedule(_currentDoneEvent, simDuration);
                _currentDoneEvent.schedule(simDuration);
            }

            //There's no Moving/RotatingDoneEvent schedules so far. So just go ahead with schedule the MovingDoneEvent
        } else {
            _currentDoneEvent = evt;
            //			((Entity)_clientObject).schedule(_currentDoneEvent, simDuration);
            _currentDoneEvent.schedule(simDuration);
        }
    }

    /**
     * Check whether there is a unscheduled Moving-/RotatingDoneEvent waiting to be scheduled. If there is, schedule
     * it.
     */
    private void handleUnscheduledDoneEvent() {
        //Check whether there is another Moving-/RotatingDoneEvent waiting to be scheduled
        if (_doneEventBuffer != null) {
            //calculate the remained duration of the stored Moving-/RotatingDoneEvent
            //			SimTime duration = SimTime.diff(_doneEventEndingTime, SimTime.NOW);
            TimeSpan duration =
                TimeOperations.diff(_doneEventEndingTime, ((ModelComponent) _clientObject).presentTime());
            assert (duration != null) : "The buffered Moving-/RotatingDoneEvent is already expired.";
            //schedule it
            _currentDoneEvent = _doneEventBuffer;
            //			((Entity)_clientObject).schedule(_currentDoneEvent, duration);
            _currentDoneEvent.schedule(duration);

            //clean up the buffer
            _doneEventBuffer = null;
            _doneEventEndingTime = null;
            //if there are no waiting event, clean up the processed current Event
        } else {
            _currentDoneEvent = null;
        }
    }


    /**
     * Informs the SpatialMovementManager that the associated object is attached to another MovableSpatialObject.
     *
     * @param host The MovableSpatialObject to attach to.
     */
    protected void attach(MovableSpatialObject host) {
        _attachHost = host;

        Matrix4d matrixHost = host.getMatrix();
        Matrix4d matrixThis = _clientObject.getMatrix();

        //get the positional difference and transform it to the local coordinate
        Vector3d vecHost = new Vector3d();
        matrixHost.get(vecHost);
        matrixThis.get(_positionOffsetDuringAttachment);
        _positionOffsetDuringAttachment.sub(vecHost);
        Matrix4d matrixHostInvert = new Matrix4d(matrixHost);
        matrixHostInvert.invert();
        matrixHostInvert.transform(_positionOffsetDuringAttachment);

        //get the rotational difference (get rotHost out of rotThis)
        Quat4d quatThis = new Quat4d();
        matrixHost.get(_orientaionOffsetDuringAttachment);
        matrixThis.get(quatThis);
        _orientaionOffsetDuringAttachment.inverse();
        _orientaionOffsetDuringAttachment.mul(quatThis);

        _eventTransmitter.fireVisualEvent(
            new AttachEvent(this, ((ModelComponent) _clientObject).getName(), ((ModelComponent) host).getName(),
                ((ModelComponent) _clientObject).presentTime()));
    }

    /**
     * Cancel the current scheduled or buffered MovingDoneEvent.
     */
    protected void cancelMovingDoneEvent() {
        if (_currentDoneEvent == null) {
            ((Entity) _clientObject).sendWarning("There's no MovingDoneEvent to be removed.",
                this + _clientObject.getName(),
                "There's no MovingDoneEvent scheduled now",
                "Be sure there's a MovingDoneEvent scheduled before calling this method.");
            return;
        }

        //check whether the current scheduled event is the MovingDoneEvent
        //if it is, cancel it and schedule the buffered event if needed
        if (_currentDoneEvent instanceof MovingDoneEvent) {
            //if the scheduled entity isn't canceled by an interrupt already
            //			if(((Entity)_clientObject).isScheduled()){
            //				((Entity)_clientObject).cancel();
            //			}
            if (_currentDoneEvent != null) {
                _currentDoneEvent.cancel();
            }
            this.handleUnscheduledDoneEvent();
            //if the current scheduled event isn't the MovingDoneEvent but the buffered event is
            //just clean up the buffer
        } else if (_doneEventBuffer != null && _doneEventBuffer instanceof MovingDoneEvent) {
            _doneEventBuffer = null;
            _doneEventEndingTime = null;
        }
    }

    /**
     * Cancel the current scheduled or buffered RotatingDoneEvent.
     */
    protected void cancelRotatingDoneEvent() {
        if (_currentDoneEvent == null) {
            ((Entity) _clientObject).sendWarning("There's no RotatingDoneEvent to be removed.",
                this + _clientObject.getName(),
                "There's no RotatingDoneEvent scheduled now",
                "Be sure there's a RotatingDoneEvent scheduled before calling this method.");
            return;
        }

        //check whether the current scheduled event is the RotatingDoneEvent
        //if it is, cancel it and schedule the buffered event if needed
        if (_currentDoneEvent instanceof RotatingDoneEvent) {
            //if the scheduled entity isn't canceled by an interrupt already
            //			if(((Entity)_clientObject).isScheduled()){
            //				((Entity)_clientObject).cancel();
            //			}
            if (_currentDoneEvent != null) {
                _currentDoneEvent.cancel();
            }
            this.handleUnscheduledDoneEvent();
            //if the current scheduled event isn't the RotatingDoneEvent but the buffered event is
            //just clean up the buffer
        } else if (_doneEventBuffer != null && _doneEventBuffer instanceof RotatingDoneEvent) {
            _doneEventBuffer = null;
            _doneEventEndingTime = null;
        }
    }

    /**
     * Notifies the SpatialMovementManager to cancel the current movement.
     */
    protected void cleanUpMovement() {
        //reset the movingStartTime
        _movingStartTime = null;
        //reset the current Movement object
        _currentMovement = null;
    }

    /**
     * Notifies the SpatialMovementManager to cancel the current rotation.
     */
    protected void cleanUpRotation() {
        //reset the rotatingStartTime
        _rotatingStartTime = null;
        //reset the current rotation
        _currentRotation = null;
    }

    /**
     * Remove attachment to a MovableSpatialObject.
     */
    protected void detach() {
        _attachHost = null;
        _positionOffsetDuringAttachment = null;
        _orientaionOffsetDuringAttachment = null;
        _eventTransmitter.fireVisualEvent(
            new DetachEvent(this, _clientObject.getName(), ((ModelComponent) _clientObject).presentTime()));
        _eventTransmitter.fireVisualEvent(new SetPositionEvent(this,
            _clientObject.getName(),
            _clientObject.getPosX().getValue(),
            _clientObject.getPosY().getValue(),
            _clientObject.getPosZ().getValue(),
            ((ModelComponent) _clientObject).presentTime()));
        Quat4d currentOrientation = new Quat4d();
        _clientObject.getMatrix().get(currentOrientation);
        _eventTransmitter.fireVisualEvent(new SetOrientationEvent(this,
            _clientObject.getName(),
            currentOrientation,
            ((ModelComponent) _clientObject).presentTime()));
    }

    /**
     * Gets the current Movement object, if it exist.
     *
     * @return The current Movement object which contains data about the current movement.
     */
    protected Movement getCurrentMovement() {
        return _currentMovement;
    }

    /**
     * Gets the current position while the object is attached.
     *
     * @return The current position.
     */
    protected double[] getCurrentPositionDuringAttachment() {
        assert (_attachHost != null) : "The MovableSpatialObject must be attached to an other.";
        Vector3d currentOffset = new Vector3d(_positionOffsetDuringAttachment);
        Matrix4d hostMatrix = _attachHost.getMatrix();

        //transform the positional offset from local to global coordinates
        //and gets the new position
        hostMatrix.transform(currentOffset);
        double[] result = new double[3];
        result[0] = hostMatrix.m03 + currentOffset.x;
        result[1] = hostMatrix.m13 + currentOffset.y;
        result[2] = hostMatrix.m23 + currentOffset.z;

        return result;
    }

    /**
     * Gets the current position on Track
     *
     * @return The current position on Track
     */
    protected double[] getCurrentPositionDuringMovement() {

        assert ((_currentMovement != null) && (_movingStartTime != null)) :
            "The object isn't moving. The position on Track can't be get.";

        return _currentMovement.getCurrentPosition(_movingStartTime, ((ModelComponent) _clientObject).presentTime());
    }

    /**
     * Gets the current Rotation object, if exists.
     *
     * @return The Rotation object which contains the data about the roation performed now.
     */
    protected Rotation getCurrentRotation() {
        return _currentRotation;
    }

    /**
     * Returns a rotation object which contains the current rotational data during a rotation.
     *
     * @return A rotation object with the rotation axis, the rotated angle and the elapsed rotation time.
     */
    protected Rotation getCurrentRotationalDataDuringRotation() {
        assert (_currentRotation != null) : "The object isn't rotating, so the current rotational data can't be get";

        //		double timeDelta = SimTime.NOW.getTimeValue()-_rotatingStartTime.getTimeValue();
        double timeDelta = ((ModelComponent) _clientObject).presentTime().getTimeAsDouble(TimeUnit.SECONDS) -
            _rotatingStartTime.getTimeAsDouble(TimeUnit.SECONDS);

        if (timeDelta > _currentRotation.getDuration()) {
            ErrorMessage eMessage = new ErrorMessage(_owner,
                "The rotational data can't be get.",
                _clientObject.getName() + ", SpatialMovementManager.getCurrentRotationalDataDuringRotation()",
                "The elapsed time is greater than the rotation duration.",
                "Recheck the code.",
                ((ModelComponent) _clientObject).presentTime());
            throw new IllegalMoveException(eMessage);
        }

        return new Rotation(_currentRotation.getAxis(),
            _currentRotation.getAngle() * (timeDelta / _currentRotation.getDuration()), timeDelta);
    }

    /**
     * Gets the current speed while the object is attached.
     *
     * @return The current speed.
     */
    protected double getCurrentSpeedDuringAttachment() {
        assert (_attachHost != null) : "The MovableSpatialObject must be attached to an other.";
        return _attachHost.getCurrentSpeed();
    }

    /**
     * Gets the current moving speed.
     *
     * @return The current moving speed.
     */
    protected double getCurrentSpeedDuringMovement() {
        assert (_currentMovement != null) : "The currentMovement must be initialized. " +
            "Otherwise the associated MovableSpatialObject should return 0. " +
            "The methods of a SpatialMovementManager should only be called by a MovableSpatialObejct.";
        return KinematicsCalculations.getCurrentSpeed(this._currentMovement, _movingStartTime,
            ((ModelComponent) _clientObject).presentTime());
    }

    /**
     * Gets the current matrix while the object is attached.
     *
     * @return The current matrix.
     */
    protected Matrix4d getMatrixDuringAttachment() {
        assert (_attachHost != null) : "The MovableSpatialObject must be attached to an other.";
        //prepare the values needed
        Matrix4d hostMatrix = _attachHost.getMatrix();
        Vector3d currentOffset = new Vector3d(_positionOffsetDuringAttachment);
        Matrix4d rotationOffset = new Matrix4d();
        rotationOffset.set(_orientaionOffsetDuringAttachment);
        Matrix4d result = new Matrix4d();

        //gets the new orientation by multiplying the host orientation with offset
        result.mul(hostMatrix, rotationOffset);

        //transform the positional offset from local to global coordinates
        //and gets the new position
        hostMatrix.transform(currentOffset);
        result.m03 = hostMatrix.m03 + currentOffset.x;
        result.m13 = hostMatrix.m13 + currentOffset.y;
        result.m23 = hostMatrix.m23 + currentOffset.z;

        return result;
    }

    /**
     * Gets the matrix with the rotational information resulted from the moving with FocusOnTack.
     *
     * @return The matrix with the rotational information.
     */
    protected Matrix4d getRotationMatrixDuringFocusOnTrack() {
        if (!_clientObject.isFocusedOnTrack()) {
            ErrorMessage eMessage = new ErrorMessage(_owner,
                "The rotation matrix can't be get.",
                _clientObject.getName() + ", SpatialMovementManagergetRotationMatrixDuringFocusOnTrack()",
                "The associated MovableSpatialObject isn't moving with FocusOnTrack option.",
                "This method is for getting the rotatoin matrix while the MovableSpatialObject is moving and focus to the moving direction.",
                ((ModelComponent) _clientObject).presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //get the moving direction
        Vector3d direction = _currentMovement.getCurrentMovingDirection(_movingStartTime,
            ((ModelComponent) _clientObject).presentTime());
        direction.normalize();

        //align the orientation in the x/z/plane first
        //then the height orientation

        //get the direction on the x/z-plane
        Vector3d directionFlat = new Vector3d(direction);
        direction.y = 0;

        //the vector which shows the to the front and the top
        Vector3d frontSide = new Vector3d(0, 0, -1);

        //get the angle between the front side and the moving direction
        double angleY = frontSide.angle(directionFlat);

        //Get the rotation axis
        Vector3d axisY = new Vector3d();
        axisY.cross(frontSide, directionFlat);

        //rotate the result matrix according to the angle and axis
        Matrix4d rotMat = new Matrix4d();
        if (axisY.y > 0) {
            rotMat.rotY(angleY);
        } else {
            rotMat.rotY(-angleY);
        }

        //now align the height
        rotMat.transform(frontSide);
        double angleX = frontSide.angle(direction);

        //get the axis
        Vector3d axisX = new Vector3d();
        axisX.cross(frontSide, direction);

        //get the result in a matrix
        Matrix4d heightOrientation = new Matrix4d();
        if (axisX.x > 0) {
            heightOrientation.rotX(angleX);
        } else {
            heightOrientation.rotX(-angleX);
        }

        //combine the orientations
        rotMat.mul(heightOrientation);

        return rotMat;
    }

    /**
     * Moves the associated MovableSpatialObject to the position of an other SpatialObject with the given duration.
     *
     * @param destination     The destination of the movement.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param simDuration     The duration of the movement in SimTime.
     */
    protected void move(SpatialObject destination, String entryPointName, TimeSpan simDuration) {
        //register when the movement is started
        _movingStartTime = ((ModelComponent) _clientObject).presentTime();

        //update the current speed.
        double currentSpeed;
        if (_currentMovement == null) {
            currentSpeed = 0.0d;
        } else {
            currentSpeed = this.getCurrentSpeedDuringMovement();
        }

        //create the current track
        Track currentTrack;
        //check whether the entry point is specified.
        if (entryPointName == null) {
            currentTrack = _layoutManager.getTrack(_clientObject, destination);
        } else {
            SpatialObject currentLocation = _clientObject.getCurrentLocation();
            if (currentLocation == null) {
                ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                        " won't be performed.",
                    _clientObject.getName() +
                        ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration)",
                    "The moving object isn't located at any SpatialObject",
                    "If the object has to move to a specific entry point of a SpatialObject" +
                        ", it must be located at SpatialObject first. Otherwise, please generate" +
                        "a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
                return;
            }
            currentTrack = _layoutManager.getTrack(currentLocation, null, destination, entryPointName);
        }
        //check whether a pre-defined track can be found.
        if (currentTrack == null) {
            ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                    " won't be performed.",
                _clientObject.getName() +
                    ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration)",
                "No pre-defined track between the current located SpatialObject and the destination " +
                    "can be found.",
                "Please generate a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
            return;
        }

        double acc = _clientObject.getAcc();
        double dec = _clientObject.getDec();
        double maxSpeed = _clientObject.getMaxSpeed();

        //move without kinematics attributes
        if (acc <= 0 || dec >= 0) {
            double speedNeeded = currentTrack.getLength() / simDuration.getTimeAsDouble(TimeUnit.SECONDS);
            if (maxSpeed > 0.0 && speedNeeded > maxSpeed) {
                //				throw new IllegalArgumentException("The given duration is too short for the movement.");
                ErrorMessage eMessage = new ErrorMessage(_owner,
                    "Can't make to the destination in time. Or the maximum speed must be exceeded.",
                    _clientObject.getName() +
                        ", SpatialMovementManager.move(SpatialObject destination, SimTime duration)",
                    "The speed needed is higher then the maximum speed limitation.",
                    "Check for new destiny or a longer duration or reconsider the consistency of the model.",
                    ((ModelComponent) _clientObject).presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _currentMovement =
                new Movement(currentTrack, new TimeSpan(0), simDuration, new TimeSpan(0), currentSpeed, speedNeeded,
                    0.0);
            //move with kinematics attributes
        } else {

            //try to fire MoveEvent if a listener is registered.
            if (maxSpeed > 0.0) {
                //if the maximum speed is given
                try {
                    _currentMovement =
                        KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                            maxSpeed, acc, dec, simDuration);
                } catch (IllegalArgumentException e) {
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "A problem occured by calculating the movement details.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration)",
                        e.getMessage(),
                        "Change the destination or duration or reconsider the consistency of the model.",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            } else {
                //if the maximum speed doesn't has to be considered
                try {
                    _currentMovement =
                        KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                            acc, dec, simDuration);
                } catch (IllegalArgumentException e) {
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "A problem occured by calculating the movement details.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration)",
                        e.getMessage(),
                        "",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            }
            _currentMovement.setTrack(currentTrack);
        }

        //try to fire MoveEvent if a listener is registered
        MoveEvent evt = new MoveEvent(this, _clientObject.getName(),
            _currentMovement, _clientObject.isFocusedOnTrack(),
            _frontSideVector,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);
        //schedule the MovingDoneEvent
        this.scheduleDoneEvent(new MovingDoneEvent(_owner, "MovingDone", _owner.traceIsOn(), _clientObject),
            simDuration);
    }

    /**
     * Moves the associated MovableSpatialObject to the position of an other SpatialObject with the given duration,
     * maximum speed, acceleration and deceleration. The character of the movement depends on the initialized
     * parameters. (O = initialized, X = not initialized(means <=0 or null)):
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
     * case 4: duration X, maxSpeed X, acc & dec O: The object will be accelerated and followed by a decdleration.
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
     *
     * @param destination     The destination of the movement.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to. (optional)
     * @param simDuration     The duration of the movement. (optional)
     * @param maxSpeed        The specific maximum speed for this movement. (optional)
     * @param acc             The specific acceleration value for this movement. (optional)
     * @param dec             The specific deceleration value for this movement. (optional)
     */
    protected void move(SpatialObject destination, String entryPointName, TimeSpan simDuration, double maxSpeed,
                        double acc, double dec) {
        //register when the movement is started
        _movingStartTime = ((ModelComponent) _clientObject).presentTime();

        //update the current speed.
        double currentSpeed;
        if (_currentMovement == null) {
            currentSpeed = 0.0d;
        } else {
            currentSpeed = this.getCurrentSpeedDuringMovement();
        }

        //create the current track
        Track currentTrack;
        //check whether the entry point is specified.
        if (entryPointName == null) {
            currentTrack = _layoutManager.getTrack(_clientObject, destination);
        } else {
            SpatialObject currentLocation = _clientObject.getCurrentLocation();
            if (currentLocation == null) {
                ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                        " won't be performed.",
                    _clientObject.getName() + ", SpatialMovementManager.move" +
                        "(SpatialObject destination, String entryPointName, TimeSpan simDuration, double maxSpeed, double acc, double dec)",
                    "The moving object isn't located at any SpatialObject",
                    "If the object has to move to a specific entry point of a SpatialObject" +
                        ", it must be located at SpatialObject first. Otherwise, please generate" +
                        "a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
                return;
            }
            currentTrack = _layoutManager.getTrack(currentLocation, null, destination, entryPointName);
        }
        //check whether a pre-defined track can be found.
        if (currentTrack == null) {
            ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                    " won't be performed.",
                _clientObject.getName() + ", SpatialMovementManager.move" +
                    "(SpatialObject destination, String entryPointName, TimeSpan simDuration, double maxSpeed, double acc, double dec)",
                "No pre-defined track between the current located SpatialObject and the destination " +
                    "can be found.",
                "Please generate a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
            return;
        }

        //determine which parameters were initialized
        int durationCode = 0;
        int maxSpeedCode = 0;
        int accCode = 0;
        if ((simDuration != null && simDuration.getTimeAsDouble(TimeUnit.SECONDS) > 0.0)) {
            durationCode = 1;
        }
        if (maxSpeed > 0.0) {
            maxSpeedCode = 3;
        }
        if (acc > 0.0 && dec < 0.0) {
            accCode = 5;
        }

        //check the combinations of parameters and initialize the movement correctly
        /* case 9: duration O, maxSpeed O, acc & dec O: The movement will be performed according to the parameters.
         * 		   If the given parameters can't be satisfied, an IllegalMoveException will be thrown.
         *
         * case 8: duration X, maxSpeed O, acc & dec O: The object will be moved to the destination with the
         * 		   fastest speed possible.
         *
         * case 6: duration O, maxSpeed X, acc & dec O: The object will move to the destination with the given
         * 		   acceleration and deceleration and try to reach the destination in time.
         * 		   If the given parameters can't be satisfied, an IllegalMoveException will be thrown.
         *
         * case 5: duration X, maxSpeed X, acc & dec O: The object will be accelerated and followed by a deceleration.
         * 		   There's no constant phase in the movement.
         *
         * case 4: duration O, maxSpeed O, acc or dec X: The object will start with the speed needed to arrive the
         * 		   destination in the given time and break suddenly to zero at the end of the movement.
         * 		   The maxSpeed gives the speed limit of the movement.
         * 		   If the given parameters can't be satisfied, an IllegalMoveException will be thrown.
         *
         * case 3: duration X, maxSpeed O, acc or dec X: The movement will start with the maxSpeed and stops
         * 		   the destination.
         *
         * case 1: duration O, maxSpeed X, acc or dec X: The movement will start with the speed needed
         * 		   to arrive the destination with the duration given.*/
        switch (durationCode + maxSpeedCode + accCode) {
            case 9:
                try {
                    _currentMovement =
                        KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                            maxSpeed, acc, dec, simDuration);
                } catch (IllegalArgumentException e) {
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "A problem occured by calculating the movement details.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration, double maxSpeed, double acc, double dec)",
                        e.getMessage(),
                        "",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            case 8:
                try {
                    _currentMovement =
                        KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                            maxSpeed, acc, dec);
                } catch (IllegalArgumentException e) {
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "A problem occured by calculating the movement details.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration, double maxSpeed, double acc, double dec)",
                        e.getMessage(),
                        "",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            case 6:
                try {
                    _currentMovement =
                        KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                            acc, dec, simDuration);
                } catch (IllegalArgumentException e) {
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "A problem occured by calculating the movement details.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration, double maxSpeed, double acc, double dec)",
                        e.getMessage(),
                        "",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            case 5:
                _currentMovement = new Movement();
                //check whether the distance is long enough for the initial speed
                double breakToZeroDistance =
                    KinematicsCalculations.getDistanceOfSpeedBoundedAcceleration(currentSpeed, 0.0d, dec);
                double totalDistance = currentTrack.getLength();

                //if the total distance is longer than the breaking to zero distance, means there is room for an acceleration
                if (breakToZeroDistance < totalDistance) {
                    double topSpeed =
                        KinematicsCalculations.getMaxAcceleratedSpeed(currentTrack.getLength(), currentSpeed, 0.0, acc,
                            dec);
                    _currentMovement.setAccDuration(new TimeSpan(
                        KinematicsCalculations.getDurationOfSpeedBoundedAcceleration(currentSpeed, topSpeed, acc)));
                    _currentMovement.setDecDuration(
                        new TimeSpan(KinematicsCalculations.getDurationOfSpeedBoundedAcceleration(topSpeed, 0.0, dec)));
                    _currentMovement.setInitialSpeed(currentSpeed);
                    _currentMovement.setMaxSpeed(topSpeed);
                    //if the total distance is exactly the breaking distance
                } else if (breakToZeroDistance == totalDistance) {
                    _currentMovement.setDecDuration(new TimeSpan(
                        KinematicsCalculations.getDurationOfSpeedBoundedAcceleration(currentSpeed, 0.0, dec)));
                    _currentMovement.setInitialSpeed(currentSpeed);
                    _currentMovement.setMaxSpeed(currentSpeed);
                    //if the total distance isn't long enough for breaking to zero
                } else {
                    //				throw new IllegalArgumentException("The initial speed is too high. It's not possible to break to zero within the given distance.");
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "Can't stop at the destination position.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration, double maxSpeed, double acc, double dec)",
                        "The distance to the destination is too short or the initial speed is too high or the deceleration value is too small.",
                        "Check for new destiny/deceleration value or reconsider the consistency of the model.",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }

            case 4:
                double speedNeeded = currentTrack.getLength() / simDuration.getTimeAsDouble(TimeUnit.SECONDS);
                if (speedNeeded > maxSpeed) {
                    //				throw new IllegalArgumentException("Can't reach the destination in the given duration.");
                    ErrorMessage eMessage = new ErrorMessage(_owner,
                        "Can't make to the destination in time. Or the maximum speed must be exceeded.",
                        _clientObject.getName() +
                            ", SpatialMovementManager.move(SpatialObject destination, SimTime simDuration, double maxSpeed, double acc, double dec)",
                        "The speed needed is higher then the maximum speed limitation.",
                        "Check for new destiny or a longer duration or reconsider the consistency of the model.",
                        ((ModelComponent) _clientObject).presentTime());
                    throw new IllegalMoveException(eMessage);
                }
                _currentMovement =
                    new Movement(null, new TimeSpan(0), simDuration, new TimeSpan(0), currentSpeed, speedNeeded, 0.0);

            case 3:
                double timeNeeded = currentTrack.getLength() / maxSpeed;
                _currentMovement =
                    new Movement(null, new TimeSpan(0), new TimeSpan(timeNeeded), new TimeSpan(0), currentSpeed,
                        maxSpeed, 0.0);

            case 1:
                double speed_Needed = currentTrack.getLength() / simDuration.getTimeAsDouble(TimeUnit.SECONDS);
                _currentMovement =
                    new Movement(null, new TimeSpan(0), simDuration, new TimeSpan(0), currentSpeed, speed_Needed, 0.0);
        }


        //set the track
        _currentMovement.setTrack(currentTrack);

        //try to fire MoveEvent if a listener is registered.
        MoveEvent evt = new MoveEvent(this, _clientObject.getName(),
            _currentMovement, _clientObject.isFocusedOnTrack(), _frontSideVector,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the MovingDoneEvent
        this.scheduleDoneEvent(new MovingDoneEvent(_owner, "MovingDone", _owner.traceIsOn(), _clientObject),
            _currentMovement.getTotalDuration());
    }


    /**
     * Moves the associated MovableSpatialObject to the position of an other SpatialObject.
     *
     * @param destination     The destination of the movement.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     */
    protected void move(SpatialObject destination, String entryPointName) {
        //register when the movement is started
        _movingStartTime = ((ModelComponent) _clientObject).presentTime();

        //update the current speed.
        double currentSpeed;
        if (_currentMovement == null) {
            currentSpeed = 0.0d;
        } else {
            currentSpeed = this.getCurrentSpeedDuringMovement();
        }

        //create the current track
        Track currentTrack;
        //check whether the entry point is specified.
        if (entryPointName == null) {
            currentTrack = _layoutManager.getTrack(_clientObject, destination);
        } else {
            SpatialObject currentLocation = _clientObject.getCurrentLocation();
            if (currentLocation == null) {
                ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                        " won't be performed.",
                    _clientObject.getName() + ", SpatialMovementManager.move" +
                        "(SpatialObject destination, String entryPointName)",
                    "The moving object isn't located at any SpatialObject",
                    "If the object has to move to a specific entry point of a SpatialObject" +
                        ", it must be located at SpatialObject first. Otherwise, please generate" +
                        "a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
                return;
            }
            currentTrack = _layoutManager.getTrack(currentLocation, null, destination, entryPointName);
        }
        //check whether a pre-defined track can be found.
        if (currentTrack == null) {
            ((ModelComponent) _clientObject).sendWarning("No matching track can be found. Move" +
                    " won't be performed.",
                _clientObject.getName() + ", SpatialMovementManager.move" +
                    "(SpatialObject destination, String entryPointName)",
                "No pre-defined track between the current located SpatialObject and the destination " +
                    "can be found.",
                "Please generate a Movement and a Track object (i.e. use SpatialLayoutManager.getSimpleTrack) manually.");
            return;
        }

        double acc = _clientObject.getAcc();
        double dec = _clientObject.getDec();
        double maxSpeed = _clientObject.getMaxSpeed();

        //move without kinematics attributes
        if (acc <= 0 || dec >= 0) {
            _currentMovement = new Movement(currentTrack, new TimeSpan(0),
                new TimeSpan(currentTrack.getLength() / maxSpeed),
                new TimeSpan(0), currentSpeed, maxSpeed, 0.0);
            //move with kinematics attributes
        } else {
            //calculates the duration needed
            try {
                _currentMovement =
                    KinematicsCalculations.configureMovement(new Movement(), currentTrack.getLength(), currentSpeed,
                        maxSpeed, acc, dec);
            } catch (IllegalArgumentException e) {
                ErrorMessage eMessage = new ErrorMessage(_owner,
                    "A problem occured by calculating the movement details.",
                    _clientObject.getName() + ", SpatialMovementManager.move(SpatialObject destination)",
                    e.getMessage(),
                    "",
                    ((ModelComponent) _clientObject).presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _currentMovement.setTrack(currentTrack);
        }

        //try to fire MoveEvent if a listener is registered
        MoveEvent evt = new MoveEvent(this, _clientObject.getName(),
            _currentMovement, _clientObject.isFocusedOnTrack(), _frontSideVector,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the MovingDoneEvent
        this.scheduleDoneEvent(new MovingDoneEvent(_owner, "MovingDone", _owner.traceIsOn(), _clientObject),
            _currentMovement.getTotalDuration());

    }


    /**
     * Move the associated MovableSpatialObject according to the given movement object.
     *
     * @param movement The movement which should be performed.
     */
    protected void move(Movement movement) {
        //register when the movement is started
        _movingStartTime = ((ModelComponent) _clientObject).presentTime();

        _currentMovement = movement;
        MoveEvent evt = new MoveEvent(this, _clientObject.getName(), _currentMovement,
            _clientObject.isFocusedOnTrack(), _frontSideVector,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the MovingDoneEvent
        this.scheduleDoneEvent(new MovingDoneEvent(_owner, "MovingDone", _owner.traceIsOn(), _clientObject),
            _currentMovement.getTotalDuration());
    }

    /**
     * Notifies the SpatialMovementManager that a movement is done and begin with the update of the coordinates.
     *
     * @param data The SpatialData which should be updated.
     * @return The updated SpatialData
     */
    protected SpatialData movingDone(SpatialData data) {

        //get the destination coordinates
        double[] destination = _currentMovement.getTrack().getDestinationValue();
        //update the new position of the SpatialData
        data.setPosition(destination[0], destination[1], destination[2]);

        if (_clientObject.isFocusedOnTrack()) {
            Matrix4d rotMat = this.getRotationMatrixDuringFocusOnTrack();
            rotMat.m03 = data.getPosX();
            rotMat.m13 = data.getPosY();
            rotMat.m23 = data.getPosZ();
            data.setMatrix(rotMat);
        }
        //reset the movingStartTime
        _movingStartTime = null;
        //reset the current Movement object
        _currentMovement = null;

        //check whether there is another unscheduled Moving-/RotatingDoneEvent
        this.handleUnscheduledDoneEvent();


        return data;
    }

    /**
     * Notifies the SpatialMovementManager that a rotation is done and begin with the update of the coordinates.
     *
     * @param data The SpatialData which should be updated.
     * @return The updated SpatialData
     */
    protected SpatialData rotatingDone(SpatialData data) {
        //get the rotation axis
        boolean[] axis = _currentRotation.getAxis();
        //check about which axis the rotation did perform
        //and rotate it with the angle if the rotation axis is found
        if (axis[0] == true) {
            data.rotX(_currentRotation.getAngle());
        }
        if (axis[1] == true) {
            data.rotY(_currentRotation.getAngle());
        }
        if (axis[2] == true) {
            data.rotX(_currentRotation.getAngle());
        }
        //reset the rotatingStartTime
        _rotatingStartTime = null;
        //reset the current rotation
        _currentRotation = null;

        //check whether there is another unscheduled Moving-/RotatingDoneEvent
        this.handleUnscheduledDoneEvent();

        return data;
    }

    /**
     * Rotate the object with the given angle and duration about the X-axis.
     *
     * @param angle    The angle to rotate about the X axis in radians.
     * @param duration The duration for the rotation.
     */
    protected void rotX(double angle, TimeSpan duration) {
        //set the rotatingStartTime
        _rotatingStartTime = ((ModelComponent) _clientObject).presentTime();
        //create the axis to rotate about
        boolean[] axis = {true, false, false};
        //the duration in double
        double durationValue = duration.getTimeAsDouble(TimeUnit.SECONDS);
        //create and save the Rotation object
        _currentRotation = new Rotation(axis, angle, durationValue);
        //try to fire the RotateEvent
        RotateEvent evt = new RotateEvent(this, _clientObject.getName(), _currentRotation,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the RotatingDoneEvent
        this.scheduleDoneEvent(new RotatingDoneEvent(_owner, "RotatingDone", _owner.traceIsOn(), _clientObject),
            duration);
    }

    /**
     * Rotate the object with the given angle and duration about the Y-axis.
     *
     * @param angle    The angle to rotate about the Y axis in radians.
     * @param duration The duration for the rotation.
     */
    protected void rotY(double angle, TimeSpan duration) {
        //set the rotatingStartTime
        _rotatingStartTime = ((ModelComponent) _clientObject).presentTime();
        //create the axis to rotate about
        boolean[] axis = {false, true, false};
        //the duration in double
        double durationValue = duration.getTimeAsDouble(TimeUnit.SECONDS);
        //create and save the Rotation object
        _currentRotation = new Rotation(axis, angle, durationValue);
        //try to fire the RotateEvent
        RotateEvent evt = new RotateEvent(this, _clientObject.getName(), _currentRotation,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the RotatingDoneEvent
        this.scheduleDoneEvent(new RotatingDoneEvent(_owner, "RotatingDone", _owner.traceIsOn(), _clientObject),
            duration);
    }

    /**
     * Rotate the object with the given angle and duration about the Z-axis.
     *
     * @param angle    The angle to rotate about the Z axis in radians.
     * @param duration The duration for the rotation.
     */
    protected void rotZ(double angle, TimeSpan duration) {
        //set the rotatingStartTime
        _rotatingStartTime = ((ModelComponent) _clientObject).presentTime();
        //create the axis to rotate about
        boolean[] axis = {false, false, true};
        //the duration in double
        double durationValue = duration.getTimeAsDouble(TimeUnit.SECONDS);
        //create and save the Rotation object
        _currentRotation = new Rotation(axis, angle, durationValue);
        //try to fire the RotateEvent
        RotateEvent evt = new RotateEvent(this, _clientObject.getName(), _currentRotation,
            ((ModelComponent) _clientObject).presentTime());
        _eventTransmitter.fireVisualEvent(evt);

        //schedule the RotatingDoneEvent
        this.scheduleDoneEvent(new RotatingDoneEvent(_owner, "RotatingDone", _owner.traceIsOn(), _clientObject),
            duration);
    }

    /**
     * Sets the front side vector in this manager.
     *
     * @param frontSideVector The front side vector to be set.
     */
    protected void setFrontSideVector(Vector3d frontSideVector) {
        _frontSideVector = frontSideVector;
    }
}
