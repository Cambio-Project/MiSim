package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;
import org.scijava.vecmath.Quat4d;
import org.scijava.vecmath.Vector3d;
import java.util.concurrent.TimeUnit;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.report.ErrorMessage;
import desmoj.core.simulator.InterruptCode;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeSpan;
import desmoj.extensions.visualEvents.MovementInterruptEvent;
import desmoj.extensions.visualEvents.RotationInterruptEvent;
import desmoj.extensions.visualEvents.SetOrientationEvent;
import desmoj.extensions.visualEvents.VisualEventTransmitter;

/**
 * The class represents the movable DESMO-J SimProcess which should be visualized. Therefore it implements the
 * MovableSpatialObject interface for the handling with the 3D spatial data, the spatial movements and the
 * parent-child-MSO concept.
 *
 * @author Fred Sun
 */
public abstract class MovableSpatialSimProcess extends SpatialSimProcess implements
    MovableSpatialObject {


    //The acceleration of the object
    private double _acceleration;

    //	//A class which contains the 3D data of the entity
    //	private SpatialData _spatialData;

    //The negative acceleration
    private double _deceleration;

    //The current location of this object
    private SpatialObject _currentLocation;

    //If the object is moving to an object, this will be the destination
    private SpatialObject _headingForLocation;

    //The singleton for sending the SpatialEvents
    private final VisualEventTransmitter _eventTransmitter;

    //The vector which point to the front side of this object
    private final Vector3d _frontSide = new Vector3d();

    //An interrupt code to cancel a scheduled movement
    private final InterruptCode _interruptCodeCancelMovement;

    //An interrupt code to cancel a scheduled rotation
    private final InterruptCode _interruptCodeCancelRotation;

    //Flag wich indicates whether the MovableSpatialSimProcess is attached to another MovableSpatialObject.
    private boolean _isAttached;

    //Flag which indicates whether the MovableSpatialSimProcess is focused on track while moving.
    private boolean _isFocusOn;

    //Flag which indicates whether the MovableSpatialSimProcess is moving or not.
    private boolean _isMoving;

    //Flag which indicates whether the MovableSpatialSimProcess is moving concurrently or not.
    private boolean _isMovingConcurrent;

    //Flag which indicates whether the MovableSpatialSimProcess is rotating or not.
    private boolean _isRotating;

    //Flag which indicates whether the MovableSpatialSimProcess is rotating concurrently or not.
    private boolean _isRotatingConcurrent;

    //The maximum reachable speed of this MovableSpatialObject
    private double _maxSpeed;

    //A class which manage the movement of this MovableSpatialSimProcess
    private final SpatialMovementManager _movementManager;

    //a link to the parent MovableSpatialObject, if exist
    //	private MovableSpatialObject _parentMSO;

    //a list containing all the child MovableSpatialObjects
    //	private ArrayList<MovableSpatialObject> _childMSO;

    /**
     * Constructs a MovableSpatialSimProcess without specify the spatial data.
     *
     * @param owner       The model this process is associated to.
     * @param name        The name of the process.
     * @param type        The type attribute belongs to the SpatialObject interface.
     * @param showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in trace. Set
     *                    to false in entity should not be shown in trace.
     */
    public MovableSpatialSimProcess(Model owner, String name, String type,
                                    boolean showInTrace) {
        super(owner, name, type, showInTrace);
        _isMoving = false;
        _isMovingConcurrent = false;
        _isFocusOn = false;
        _isRotating = false;
        _isRotatingConcurrent = false;
        _isAttached = false;
        _movementManager = new SpatialMovementManager(owner, this);
        _interruptCodeCancelMovement = new InterruptCode("Cancel the Movement");
        _interruptCodeCancelRotation = new InterruptCode("Cancel the Rotation");
        //		_childMSO = new ArrayList<MovableSpatialObject>();
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
        //		_spatialData = new SpatialData();
    }

    /**
     * Constructs a MovableSpatialSimProcess with specific acceleration and negative acceleration, maximum speed and
     * position data.
     *
     * @param owner       The model this entity is associated to.
     * @param name        The name of the entity.
     * @param type        The type attribute belongs to the SpatialObject interface.
     * @param showInTrace showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in
     *                    trace. Set to false in entity should not be shown in trace..
     * @param acc         The acceleration of this MovableSpatialObject.
     * @param dec         The breaking acceleration of this MovableSpatialObject (normally a negative value).
     * @param mSpeed      The maximum speed of this MobavleSpatialObject.
     */
    public MovableSpatialSimProcess(Model owner, String name,
                                    String type, boolean showInTrace, double acc, double dec,
                                    double mSpeed) {
        super(owner, name, type, showInTrace);
        _isMoving = false;
        _isMovingConcurrent = false;
        _isFocusOn = false;
        _isRotating = false;
        _isRotatingConcurrent = false;
        _isAttached = false;
        _movementManager = new SpatialMovementManager(owner, this);
        _acceleration = acc;
        _deceleration = dec;
        _maxSpeed = mSpeed;
        _interruptCodeCancelMovement = new InterruptCode("Cancel the Movement");
        _interruptCodeCancelRotation = new InterruptCode("Cancel the Rotation");
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
    }

    /**
     * Constructs a MovableSpatialSimProcess with specific acceleration and negative acceleration, maximum speed,
     * position data and SpatialMovementManager for movement customizing.
     *
     * @param owner          The model this entity is associated to.
     * @param name           The name of the entity.
     * @param type           The type attribute belongs to the SpatialObject interface.
     * @param showInTrace    showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up
     *                       in trace. Set to false in entity should not be shown in trace.
     * @param acc            The acceleration of this MovableSpatialObject.
     * @param dec            The breaking acceleration of this MovableSpatialObject (normally a negative value)
     * @param mSpeed         The maximum speed of this MovableSpatialObject.
     * @param startPositionX The x start position in ExtendedLength.
     * @param startPositionY The y start position in ExtendedLength.
     * @param startPositionZ The z start position in ExtendedLength.
     * @param manager        a specific SpatialMovementManager which the user could have customized.
     */
    public MovableSpatialSimProcess(Model owner, String name, String type,
                                    boolean showInTrace, double acc, double dec,
                                    double mSpeed, Length startPositionX, Length startPositionY, Length startPositionZ,
                                    SpatialMovementManager manager) {
        super(owner, name, type, showInTrace, startPositionX, startPositionY, startPositionZ);
        _isMoving = false;
        _isMovingConcurrent = false;
        _isFocusOn = false;
        _isRotating = false;
        _isRotatingConcurrent = false;
        _isAttached = false;
        _movementManager = manager;
        _acceleration = acc;
        _deceleration = dec;
        _maxSpeed = mSpeed;
        _interruptCodeCancelMovement = new InterruptCode("Cancel the Movement");
        _interruptCodeCancelRotation = new InterruptCode("Cancel the Rotation");
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
    }

    //CONSTRUCTORS WITH FRONT SIDE VECTOR AND PARENT-MSO FEATURES
    //	/**
    //	 * Constructs a MovableSpatialSimProcess with specific acceleration and negative acceleration, maximum speed and position data.
    //	 * @param owner The model this entity is associated to.
    //	 * @param name The name of the entity.
    //	 * @param type The type attribute belongs to the SpatialObject interface.
    //	 * @param showInTrace showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in trace. Set to false in entity should not be shown in trace..
    //	 * @param acc The acceleration of this MovableSpatialObject.
    //	 * @param dec The breaking acceleration of this MovableSpatialObject (normally a negative value).
    //	 * @param mSpeed The maximum speed of this MobavleSpatialObject.
    //	 * @param frontSide The vector which points to the front side of the object. (This is only needed if the focusOnTrack option of the move methods should be used.)
    //	 * @param parentMSO The parent-MovableSpatialObject (if this object should be the child-MSO of it).
    //	 */
    //	public MovableSpatialSimProcess(Model owner, String name, String type,
    //			boolean showInTrace, double acc, double dec,
    //			double mSpeed, Vector3d frontSide, MovableSpatialObject parentMSO){
    //		super(owner, name, type, showInTrace);
    //		_isMoving = false;
    //		_isMovingConcurrent = false;
    //		_isFocusOn = false;
    //		_isRotating = false;
    //		_isRotatingConcurrent = false;
    //		_isAttached = false;
    //		_movementManager = new SpatialMovementManager(owner,this);
    //		_acceleration = acc;
    //		_deceleration = dec;
    //		_maxSpeed = mSpeed;
    //		_interruptCodeCancelMovement = new InterruptCode("Cancel the Movement");
    //		_interruptCodeCancelRotation = new InterruptCode("Cancel the Rotation");
    //		if(frontSide!=null){
    //			_frontSide = frontSide;
    //			_frontSide.normalize();
    //			_movementManager.setFrontSideVector(_frontSide);
    //		}
    //		if(parentMSO!=null){
    //			_parentMSO = parentMSO;
    //			_parentMSO.addChildMSO(this);
    //		}
    //		_childMSO = new ArrayList<MovableSpatialObject>();
    //		_eventTransmitter =  VisualEventTransmitter.getSpatialEventTransmitter();
    //	}
    //
    //	/**
    //	 * Constructs a MovableSpatialSimProcess with specific acceleration and negative acceleration, maximum speed, position data and SpatialMovementManager for movement customizing.
    //	 * @param owner The model this entity is associated to.
    //	 * @param name The name of the entity.
    //	 * @param type The type attribute belongs to the SpatialObject interface.
    //	 * @param showInTrace showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in trace. Set to false in entity should not be shown in trace.
    //	 * @param acc The acceleration of this MovableSpatialObject.
    //	 * @param dec The breaking acceleration of this MovableSpatialObject (normally a negative value)
    //	 * @param mSpeed The maximum speed of this MovableSpatialObject.
    //	 * @param startPositionX The x start position in ExtendedLength.
    //	 * @param startPositionY The y start position in ExtendedLength.
    //	 * @param startPositionZ The z start position in ExtendedLength.
    //	 * @param frontSide The vector which points to the front side of the object. (This is only needed if the focusOnTrack option of the move methods should be used.)
    //	 * @param parentMSO The parent-MovableSpatialObject (if this object should be the child-MSO of it).
    //	 * @param manager a specific SpatialMovementManager which the user could have customized.
    //	 */
    //	public MovableSpatialSimProcess(Model owner, String name, String type,
    //			boolean showInTrace, double acc, double dec,
    //			double mSpeed, ExtendedLength startPositionX, ExtendedLength startPositionY, ExtendedLength startPositionZ, Vector3d frontSide,
    //			MovableSpatialObject parentMSO, SpatialMovementManager manager){
    //		super(owner, name, type, showInTrace, startPositionX, startPositionY, startPositionZ);
    //		_isMoving = false;
    //		_isMovingConcurrent = false;
    //		_isFocusOn = false;
    //		_isRotating = false;
    //		_isRotatingConcurrent = false;
    //		_isAttached = false;
    //		_movementManager = manager;
    //		_acceleration = acc;
    //		_deceleration = dec;
    //		_maxSpeed = mSpeed;
    //		_interruptCodeCancelMovement = new InterruptCode("Cancel the Movement");
    //		_interruptCodeCancelRotation = new InterruptCode("Cancel the Rotation");
    //		if(frontSide!=null){
    //			_frontSide = frontSide;
    //			_frontSide.normalize();
    //			_movementManager.setFrontSideVector(_frontSide);
    //		}
    //		if(parentMSO!=null){
    //			_parentMSO = parentMSO;
    //			_parentMSO.addChildMSO(this);
    //		}
    //		_childMSO = new ArrayList<MovableSpatialObject>();
    //		_eventTransmitter =  VisualEventTransmitter.getSpatialEventTransmitter();
    //	}

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#addchildMSO(desmoj.extensions.space3D.MovableSpatialObject)
    //	 */
    //	@Override
    //	public void addChildMSO(MovableSpatialObject childMSO){
    //		if(!childMSO.getParentMSO().equals(this)){
    //			this.sendWarning("This can't add the given MovableSpatialObject as childMSO.",
    //					this.getName()+" addchildMSO(MovableSpatialObject childMSO)",
    //					"The given MovableSpatialObject doesn't have this object as its parent-MSO.",
    //					"A child-MSO should be deklared at the creation of it. " +
    //					"This method should only be called by a MovableSpatialObject's constructor.");
    //			//do nothing, because a child-MSO only move in its own coordinate system.
    //			return;
    //		}
    //		_childMSO.add(childMSO);
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#attach(desmoj.extensions.space3D.MovableSpatialObject)
     */
    public void attach(MovableSpatialObject host) {
        if (_isMoving || _isRotating || _isMovingConcurrent || _isRotatingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "Attachment not allowed now.",
                this.getName() + ", MovableSpatialSimProcess.attach(MovableSpatialObject host)",
                "The object is moving or rotating right now.",
                "This object can only attach to another when no movement or rotation isn't being performed.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //do nothing if this MSO is a childMSO. it's already linked to a parent MSO permanently
        //		if(this.isChildMSO()){
        //			return;
        //		}
        if (_isAttached) {
            this.detach();
        }
        _movementManager.attach(host);
        _isAttached = true;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#cancelMovement()
     */
    public void cancelMovement() {
        if (_isMoving) {
            //interrupt and wake up the move method
            this.interrupt(_interruptCodeCancelMovement);
            //the interrupted move method will handle the rest
        } else if (_isMovingConcurrent) {
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (_isFocusOn) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            //cancel the registered MovingDoneEvent
            _movementManager.cancelMovingDoneEvent();

            //clean up all flags in this class and in the SpatialMovementManager
            _isMoving = false;
            _isMovingConcurrent = false;
            _isFocusOn = false;
            _headingForLocation = null;
            _movementManager.cleanUpMovement();
        } else {
            this.sendWarning("The movement can't be canceled.",
                this.getName() + " cancelMovement()",
                "The object isn't moving.",
                "Only if this object is moving its movement can be canceled.");
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#cancelRotation()
     */
    public void cancelRotation() {
        if (_isRotating) {
            //interrupt and wake up the rotate method
            this.interrupt(_interruptCodeCancelRotation);
            //the interrupted rotate method will handle the rest
        } else if (_isRotatingConcurrent) {
            //update the current rotational data
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }

            //cancel the registered RotatingDoneEvent
            _movementManager.cancelRotatingDoneEvent();

            //clean up all flags in this class and in the SpatialMovementManager
            _isRotating = false;
            _isRotatingConcurrent = false;
            _movementManager.cleanUpRotation();
        } else {
            this.sendWarning("The rotation can't be canceled.",
                this.getName() + " cancelRotation()",
                "The object isn't rotating.",
                "Only if this object is rotating its rotation can be canceled.");
        }
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#containschildMSO(desmoj.extensions.space3D.MovableSpatialObject)
    //	 */
    //	@Override
    //	public boolean containsChildMSO(MovableSpatialObject childMSO){
    //		return _childMSO.contains(childMSO);
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#detach()
     */
    public void detach() {
        if (_isAttached) {
            _isAttached = false;
            _spatialData.setMatrix(_movementManager.getMatrixDuringAttachment());
            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _movementManager.detach();
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getAcc()
     */
    public double getAcc() {
        return _acceleration;
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#getAllchildMSO()
    //	 */
    //	@Override
    //	public Iterator<MovableSpatialObject> getAllChildMSO(){
    //		return _childMSO.iterator();
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#setAcc(double)
     */
    public void setAcc(double acc) {
        _acceleration = acc;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getCurrentLocation()
     */
    public SpatialObject getCurrentLocation() {
        return _currentLocation;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getCurrentMovement()
     */
    public Movement getCurrentMovement() {
        if (_isMoving || _isMovingConcurrent) {
            return _movementManager.getCurrentMovement();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getCurrentRotation()
     */
    public Rotation getCurrentRotation() {
        return _movementManager.getCurrentRotation();
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getCurrentSpeed()
     */
    public double getCurrentSpeed() {
        if (_isAttached) {
            return _movementManager.getCurrentSpeedDuringAttachment();
        } else if (_isMoving || _isMovingConcurrent) {
            return _movementManager.getCurrentSpeedDuringMovement();
        } else {
            return 0.0;
        }
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#getFrontSideVector()
    //	 */
    //	@Override
    //	public Vector3d getFrontSideVector(){
    //		//if there is anything what could control the orientation of this object
    //		//give the updated front side vector back
    //		if((_isFocusOn||_isRotating||_isAttached)&&_frontSide!=null){
    //			Vector3d result = _frontSide;
    //			this.getMatrix().transform(result);
    //			return result;
    //		}
    //		return _frontSide;
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getDec()
     */
    public double getDec() {
        return _deceleration;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#setDec(double)
     */
    public void setDec(double dec) {
        _deceleration = dec;
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#getNumberOfchildMSO()
    //	 */
    //	@Override
    //	public int getNumberOfChildMSO(){
    //		return _childMSO.size();
    //	}

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#getParentMSO()
    //	 */
    //	@Override
    //	public MovableSpatialObject getParentMSO(){
    //		return _parentMSO;
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#getMatrix()
     */
    public Matrix4d getMatrix() {
        //get the current spatial data
        Matrix4d result = _spatialData.getMatrix();

        //if attachment, return the updated matrix
        if (_isAttached) {
            result = _movementManager.getMatrixDuringAttachment();
            //			_spatialData.setMatrix(result);
            //			if(_frontSide!=null){
            //				//update the front side vector
            //				_spatialData.getMatrix().transform(_frontSide);
            //			}
            return result;
        }

        //if the object is moving, update the position
        if (_isMoving || _isMovingConcurrent) {
            double[] currentPosition = _movementManager.getCurrentPositionDuringMovement();
            //if focusOn, also update the rotational data
            if (_isFocusOn) {
                result = _movementManager.getRotationMatrixDuringFocusOnTrack();
            }
            result.m03 = currentPosition[0];
            result.m13 = currentPosition[1];
            result.m23 = currentPosition[2];

            //			_spatialData.setPosition(currentPosition[0], currentPosition[1], currentPosition[2]);
            //			if(_isFocusOn){
            //				//set the matrix with the new orientation
            //				Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
            //				rotMat.m03 = _spatialData.getPosX();
            //				rotMat.m13 = _spatialData.getPosY();
            //				rotMat.m23 = _spatialData.getPosZ();
            //				_spatialData.setMatrix(rotMat);
            //				//update the front side vector
            //				rotMat.transform(_frontSide);
            //			}
        }

        //if rotating, update the rotational data
        if (_isRotating || _isRotatingConcurrent) {
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                result.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                result.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                result.rotZ(rotation.getAngle());
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#getMaxSpeed()
     */
    public double getMaxSpeed() {
        return _maxSpeed;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#setMaxSpeed(double)
     */
    public void setMaxSpeed(double mSpeed) {
        _maxSpeed = mSpeed;
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#haschildMSO()
    //	 */
    //	@Override
    //	public boolean hasChildMSO(){
    //		return _childMSO.isEmpty();
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#getPosX()
     */
    @Override
    public Length getPosX() {
        if (_isAttached) {
            return new Length(_movementManager.getCurrentPositionDuringAttachment()[0]);
        } else if (_isMoving || _isMovingConcurrent) {
            return new Length(_movementManager.getCurrentPositionDuringMovement()[0]);
        } else {
            return new Length(_spatialData.getPosX());
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#getPosY()
     */
    public Length getPosY() {
        if (_isAttached) {
            return new Length(_movementManager.getCurrentPositionDuringAttachment()[1]);
        } else if (_isMoving || _isMovingConcurrent) {
            return new Length(_movementManager.getCurrentPositionDuringMovement()[1]);
        } else {
            return new Length(_spatialData.getPosY());
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#getPosZ()
     */
    public Length getPosZ() {
        if (_isAttached) {
            return new Length(_movementManager.getCurrentPositionDuringAttachment()[2]);
        } else if (_isMoving || _isMovingConcurrent) {
            return new Length(_movementManager.getCurrentPositionDuringMovement()[2]);
        } else {
            return new Length(_spatialData.getPosZ());
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#isAttached()
     */
    public boolean isAttached() {
        return _isAttached;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#isFocusedOnTrack()
     */
    public boolean isFocusedOnTrack() {
        return _isFocusOn;
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#ischildMSO()
    //	 */
    //	@Override
    //	public boolean isChildMSO(){
    //		if (_parentMSO == null){
    //			return false;
    //		}
    //		else{
    //			return true;
    //		}
    //	}

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#isMoving()
     */
    public boolean isMoving() {
        return _isMoving;
    }

    /**
     * Shows whether this object is moving concurrentlly.
     *
     * @return True, if it's moving concurrentlly. Else, false.
     */
    public boolean isMovingConcurrent() {
        return _isMovingConcurrent;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#isRotating()
     */
    public boolean isRotating() {
        return _isRotating;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#move(desmoj.extensions.space3D.SpatialObject, java.lang.String, boolean)
     */
    public void move(SpatialObject destination, String entryPointName, boolean focusOnTrack) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isRotatingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be moved.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
        //					"The object is already rotating concurrently.",
        //					"Cancel the existed concurrent rotation or wait until it stops before start a non-concurrent movement.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (_maxSpeed <= 0.0) {
            //			throw new IllegalArgumentException("The maximum speed must be initialized for this method.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The maximum speed is invalid or not initialized.",
                this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                "The maxSpeed of the MovableSpatialObject is smaller or equal 0.",
                "For using this method the maxSpeed of the MovableSpatialObejct must be greater than 0.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() + ", MovableSpatialSimProcess.move(SpatialObject destination, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        _movementManager.move(destination, entryPointName);
        _isMoving = true;
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
        this.passivate();
        //if the movement is interrupted, update the position (and orientation) and clean up the flags in this class and in the movementManager
        if (isInterrupted()) {
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _isMoving = false;
            _isFocusOn = false;
            _headingForLocation = null;
            //if MovingDoneEvent is scheduled after a RotatingDoneEvent in SpatialMovementManager, cancel it
            _movementManager.cancelMovingDoneEvent();
            _movementManager.cleanUpMovement();
            //clean up the interrupt code if the movement is canceled by cancelMovement()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelMovement)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#move(desmoj.extensions.space3D.SpatialObject, java.lang.String, desmoj.core.simulator.TimeSpan, boolean)
     */
    public void move(SpatialObject destination, String entryPointName, TimeSpan duration, boolean focusOnTrack)
        throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isRotatingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be moved.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
        //					"The object is already rotating concurrently.",
        //					"Cancel the existed concurrent rotation or wait until it stops before start a non-concurrent movement.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, TimeSpan duration, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (duration == null) {
            //			throw new IllegalArgumentException("The duration can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The duration unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The duration is null.",
                "The duration can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (duration.getTimeAsDouble(TimeUnit.SECONDS) <= 0.0) {
            //			throw new IllegalArgumentException("The duration must greater than 0.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The duration is invalid.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The time value of the SimTime object is smaller or equal 0.",
                "The time value must be greater than 0.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        _movementManager.move(destination, entryPointName, duration);
        _isMoving = true;
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
        this.passivate();
        //if the movement is interrupted, update the position (and orientation) and clean up the flags in this class and in the movementManager
        if (isInterrupted()) {
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _isMoving = false;
            _isFocusOn = false;
            _headingForLocation = null;
            //if MovingDoneEvent is scheduled after a RotatingDoneEvent in SpatialMovementManager, cancel it
            _movementManager.cancelMovingDoneEvent();
            _movementManager.cleanUpMovement();
            //clean up the interrupt code if the movement is canceled by cancelMovement()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelMovement)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#move(desmoj.extensions.space3D.SpatialObject, java.lang.String, desmoj.core.simulator.TimeSpan, double, double, double, boolean)
     */
    public void move(SpatialObject destination, String entryPointName, TimeSpan duration, double maxSpeed, double acc,
                     double dec, boolean focusOnTrack) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isRotatingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be moved.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
        //					"The object is already rotating concurrently.",
        //					"Cancel the existed concurrent rotation or wait until it stops before start a non-concurrent movement.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.move(SpatialObject destination, TimeSpan duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if ((duration == null || duration.getTimeAsDouble(TimeUnit.SECONDS) <= 0.0) && (maxSpeed <= 0.0) &&
            (acc <= 0.0 || dec <= 0.0)) {
            //			throw new IllegalArgumentException("Insufficient parameters. At least one of duration/maximum speed/acc,dec attributes must be given.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The arguments insufficient initialized.",
                this.getName() +
                    ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "All the arguments are not initialized or invalid.",
                "At least on of these attributes must be specified: duration, maximum speed or acceleration/deceleration.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        _movementManager.move(destination, entryPointName, duration, maxSpeed, acc, dec);
        _isMoving = true;
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
        this.passivate();
        //if the movement is interrupted, update the position (and orientation) and clean up the flags in this class and in the movementManager
        if (isInterrupted()) {
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _isMoving = false;
            _isFocusOn = false;
            _headingForLocation = null;
            //if MovingDoneEvent is scheduled after a RotatingDoneEvent in SpatialMovementManager, cancel it
            _movementManager.cancelMovingDoneEvent();
            _movementManager.cleanUpMovement();
            //clean up the interrupt code if the movement is canceled by cancelMovement()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelMovement)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#move(desmoj.extensions.space3D.Movement, boolean)
     */
    public void move(Movement movement, boolean focusOnTrack) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(Movement movement, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(Movement movement, boolean focusOnTrack)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.move(Movement movement, boolean focusOnTrack)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isRotatingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be moved.",
        //					this.getName()+", MovableSpatialSimProcess.move(Movement movement, boolean focusOnTrack)",
        //					"The object is already rotating concurrently.",
        //					"Cancel the existed concurrent rotation or wait until it stops before start a non-concurrent movement.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        //check whether the movement object is valid and ready to move
        if (movement == null || !movement.isValid()) {
            //			throw new IllegalArgumentException("The movements detail isn't specified enough.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The Movement object is invalid.",
                this.getName() + ", MovableSpatialEntity.move(Movement movement, boolean focusOnTrack)",
                "The Movement object is invalid or null.",
                "The can't be null. For checking whether a movement is valid, please check Movement.inValid() and its doc.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() + ", MovableSpatialSimProcess.move(Movement movement, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        _movementManager.move(movement);
        _isMoving = true;
        //leave the current location
        _currentLocation = null;
        this.passivate();
        //if the movement is interrupted, update the position (and orientation) and clean up the flags in this class and in the movementManager
        if (isInterrupted()) {
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _isMoving = false;
            _isFocusOn = false;
            //if MovingDoneEvent is scheduled after a RotatingDoneEvent in SpatialMovementManager, cancel it
            _movementManager.cancelMovingDoneEvent();
            _movementManager.cleanUpMovement();
            //clean up the interrupt code if the movement is canceled by cancelMovement()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelMovement)) {
                this.clearInterruptCode();
            }
        }
    }

    /**
     * Move the object to the position of another SpatialObject without a specified duration. The difference to the move
     * methods is that this method won't passivate the MovableSpatialSimProcess after the execution so other actions
     * like a rotation can be performed while the object is moving. If the object is already moving it will move to the
     * new destination. It use the acceleration, deceleration and the maximum speed of the MovableSpatialObject. For
     * this method the maximum speed of the object must be given or an IllegalMoveException will be thrown. If the
     * acceleration OR deceleration of the object is <= 0, then the kinematical attributes won't be calculated and the
     * movement will start with the maximum speed and break to zero immediately at the destination.
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    public void moveConcurrent(SpatialObject destination, String entryPointName, boolean focusOnTrack) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (_maxSpeed <= 0.0) {
            //			throw new IllegalArgumentException("The maximum speed must be initialized for this method.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The maximum speed is invalid or not initialized.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                "The maxSpeed of the MovableSpatialObject is smaller or equal 0.",
                "For using this method the maxSpeed of the MovableSpatialObejct must be greater than 0.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            if (_isRotatingConcurrent) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, boolean focusOnTrack)",
                    "The focusOnTrack function can't be activated while a ratation is performing.",
                    "Cancel the rotation or wait until the rotation is finished before using the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        if (_isMovingConcurrent) {
            _movementManager.cancelMovingDoneEvent();
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _movementManager.move(destination, entryPointName);
        } else {
            _movementManager.move(destination, entryPointName);
            _isMovingConcurrent = true;
        }
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
    }

    /**
     * Move the object to the position of another SpatialObject with a specified duration. The difference to the move
     * methods is that this method won't passivate the MovableSpatialSimProcess after the execution so other actions
     * like a rotation can be performed while the object is moving. If the object is already moving it will move to the
     * new destination. It use the acceleration, deceleration and the maximum speed of the MovableSpatialObject. If the
     * acceleration OR deceleration of the object is <= 0, then the kinematical attributes won't be calculated and the
     * movement will start with the speed needed to arrive the destination in time. If the maximum speed of the object
     * is initialized (>0), then an IllegalMoveException will be thrown if it is not possible to make to the destination
     * in time.
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to.
     * @param duration        The duration of the movement in TimeSpan.
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    public void moveConcurrent(SpatialObject destination, String entryPointName, TimeSpan duration,
                               boolean focusOnTrack) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, TimeSpan duration, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (duration == null) {
            //			throw new IllegalArgumentException("The duration can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The duration unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The duration is null.",
                "The duration can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (duration.getTimeAsDouble(TimeUnit.SECONDS) <= 0.0) {
            //			throw new IllegalArgumentException("The duration must greater than 0.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The duration is invalid.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                "The time value of the SimTime object is smaller or equal 0.",
                "The time value must be greater than 0.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            if (_isRotatingConcurrent) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, boolean focusOnTrack)",
                    "The focusOnTrack function can't be activated while a ratation is performing.",
                    "Cancel the rotation or wait until the rotation is finished before using the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        if (_isMovingConcurrent) {
            _movementManager.cancelMovingDoneEvent();
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _movementManager.move(destination, entryPointName, duration);
        } else {
            _movementManager.move(destination, entryPointName, duration);
            _isMovingConcurrent = true;
        }
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
    }

    /**
     * Move the object to the position of another SpatialObject with specific attributes. The difference to the move
     * methods is that this method won't passivate the MovableSpatialSimProcess after the execution so other actions
     * like a rotation can be performed while the object is moving. If the object is already moving it will move to the
     * new destination. There are different varieties of movement modes depends on the initialized parameters (O =
     * initialized, X = not initialized(means <=0 or null)):
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
     *
     * @param destination     The destination object we want to move to.
     * @param enttryPointName The name of the specific entry point at the destination object, which should be steered
     *                        to(optional).
     * @param duration        The duration of the movement in TimeSpan(optional).
     * @param maxSpeed        The maximum speed of the movement(optional).
     * @param acc             The acceleration of the movement(optional).
     * @param dec             The deceleration of the movement(optional).
     * @param focusOnTrack    Whether the orientation of the moving object should focus to the moving direction.
     */
    public void moveConcurrent(SpatialObject destination, String entryPointName, TimeSpan duration, double maxSpeed,
                               double acc, double dec, boolean focusOnTrack) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (destination == null) {
            //			throw new IllegalArgumentException("The destination can't be null.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The destination unspecified.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "The destination is null.",
                "The destination can't be null.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(this.isChildMSO()){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"This MovableSpatialObject can't move to another MovableSpatialObject.",
        //					this.getName()+", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
        //					"This Object is a child-MovableSpatialObejct.",
        //					"A child-MSO can't have another MSO as destination.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if ((duration == null || duration.getTimeAsDouble(TimeUnit.SECONDS) <= 0.0) && (maxSpeed <= 0.0) &&
            (acc <= 0.0 || dec <= 0.0)) {
            //			throw new IllegalArgumentException("Insufficient parameters. At least one of duration/maximum speed/acc,dec attributes must be given.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The arguments insufficient initialized.",
                this.getName() +
                    ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                "All the arguments are not initialized or invalid.",
                "At least on of these attributes must be specified: duration, maximum speed or acceleration/deceleration.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            if (_isRotatingConcurrent) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.move(SpatialObject destination, SimTime duration, double maxSpeed, double acc, double dec, boolean focusOnTrack)",
                    "The focusOnTrack function can't be activated while a ratation is performing.",
                    "Cancel the rotation or wait until the rotation is finished before using the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        if (_isMovingConcurrent) {
            _movementManager.cancelMovingDoneEvent();
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

            }

            _movementManager.move(destination, entryPointName, duration, maxSpeed, acc, dec);
        } else {
            _movementManager.move(destination, entryPointName, duration, maxSpeed, acc, dec);
            _isMovingConcurrent = true;
        }
        //leave the current location and move to the next
        _currentLocation = null;
        _headingForLocation = destination;
    }

    /**
     * Move the object to the destination according to the given movement object. The difference to the move methods is
     * that this method won't passivate the MovableSpatialSimProcess after the execution so other actions like a
     * rotation can be performed while the object is moving. If the object is already moving it will move to the new
     * destination.
     *
     * @param movement     The movement which should be performed.
     * @param focusOnTrack Whether the orientation of the moving object should focus to the moving direction.
     */
    public void moveConcurrent(Movement movement, boolean focusOnTrack) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before move it.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be moved.",
                this.getName() + ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent movement.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //check whether the movement object is valid and ready to move
        if (movement == null || !movement.isValid()) {
            //			throw new IllegalArgumentException("The movements detail isn't specified enough.");
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The Movement object is invalid.",
                this.getName() + ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                "The Movement object is invalid or null.",
                "The can't be null. For checking whether a movement is valid, please check Movement.inValid() and its doc.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (focusOnTrack) {
            if (_frontSide == null) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                    "The front side vector isn't initialized.",
                    "The front side vector of this MovableSpatialObject must be initialized for the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            if (_isRotatingConcurrent) {
                ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                    "The focusOnTrack can't be activated.",
                    this.getName() +
                        ", MovableSpatialSimProcess.moveConcurrent(Movement movement, boolean focusOnTrack)",
                    "The focusOnTrack function can't be activated while a ratation is performing.",
                    "Cancel the rotation or wait until the rotation is finished before using the focusOnTrack function.",
                    this.presentTime());
                throw new IllegalMoveException(eMessage);
            }
            _isFocusOn = true;
        }
        if (_isMovingConcurrent) {
            _movementManager.cancelMovingDoneEvent();
            double[] pos = _movementManager.getCurrentPositionDuringMovement();
            if (focusOnTrack) {
                //update the rotational data if focusOnTrack
                Matrix4d rotMat = _movementManager.getRotationMatrixDuringFocusOnTrack();
                rotMat.m03 = pos[0];
                rotMat.m13 = pos[1];
                rotMat.m23 = pos[2];
                _spatialData.setMatrix(rotMat);
                rotMat.transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));

                //fire an setOrientationEvent
                Quat4d orientation = new Quat4d();
                rotMat.get(orientation);
                _eventTransmitter.fireVisualEvent(
                    new SetOrientationEvent(this, this.getName(), orientation, this.presentTime()));
            } else {
                //or just update the positional data
                _spatialData.setPosition(pos[0], pos[1], pos[2]);

                //fire an interruptEvent
                _eventTransmitter.fireVisualEvent(
                    new MovementInterruptEvent(this, this.getName(), pos, this.presentTime()));
            }

            _movementManager.move(movement);
        } else {
            _movementManager.move(movement);
            _isMovingConcurrent = true;
        }
        //leave the current location
        _currentLocation = null;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#notifyMovingDone()
     */
    public void notifyMovingDone() throws SuspendExecution {
        if (_isMoving == false && _isMovingConcurrent == false) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.notifyMovingDone()",
                "The object isn't moving.",
                "This method can't only be called if the object is moving. Check the code.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        _spatialData = _movementManager.movingDone(_spatialData);

        //clean up the flags
        if (_isFocusOn) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
            _isFocusOn = false;
        }

        //check whether the move or the moveConcurrent method was called
        if (_isMoving) {
            _isMoving = false;
            this.activate(this.presentTime());
        } else {
            _isMovingConcurrent = false;
        }
        _currentLocation = _headingForLocation;
        _headingForLocation = null;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#notifyRotatingDone()
     */
    public void notifyRotatingDone() throws SuspendExecution {
        if (_isRotating == false && _isRotatingConcurrent == false) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.notifyRotatingDone()",
                "The object isn't rotating.",
                "This method can't only be called if the object is rotating. Check the code.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        _spatialData = _movementManager.rotatingDone(_spatialData);
        if (_frontSide != null) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
        }

        //check whether the rotate or the rotateConcurrent method was called and clean up the flags
        if (_isRotating) {
            _isRotating = false;
            this.activate(this.presentTime());
        } else {
            _isRotatingConcurrent = false;
        }

    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#resetRotation()
     */
    public void resetRotation() {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.resetRotation()",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating || _isRotatingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The rotation can't be reset.",
                this.getName() + ", MovableSpatialSimProcess.resetRotation()",
                "The object is already rotating.",
                "The rotational property can't be set if the object is already rotating.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.resetRotation()",
                "The object is moving with focusOnTrack function. The orientation of the MovableSpatialEntity can't be reset now.",
                "Finish the movement with focusOnTrack function before reset the rotation of this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        super.resetRotation();
        if (_frontSide != null) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
        }
        //		_spatialData.resetRotation();
        //		_movementManager.resetRotation();

    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#rotX(double, desmoj.core.simulator.TimeSpan)
     */
    public void rotX(double angle, TimeSpan duration) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isMovingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be rotated.",
        //					this.getName()+", MovableSpatialSimProcess.rotX(double angle, SimTime duration)",
        //					"The object is already moving concurrently.",
        //					"Cancel the existed concurrent movement or wait until it stops before start a non-concurrent rotation.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle, SimTime duration)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating || _isRotatingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle, SimTime duration)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        _movementManager.rotX(angle, duration);
        _isRotating = true;
        this.passivate();
        if (isInterrupted()) {
            //update the rotation and cancel the rotation in this class and in the SpatialMovementManager
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _isRotating = false;
            _movementManager.cancelRotatingDoneEvent();
            _movementManager.cleanUpRotation();
            //clean up the interrupt code if the rotation is canceled by cancelRotation()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelRotation)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#rotY(double, desmoj.core.simulator.TimeSpan)
     */
    public void rotY(double angle, TimeSpan duration) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isMovingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be rotated.",
        //					this.getName()+", MovableSpatialSimProcess.rotY(double angle, SimTime duration)",
        //					"The object is already moving concurrently.",
        //					"Cancel the existed concurrent movement or wait until it stops before start a non-concurrent rotation.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle, SimTime duration)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating || _isRotatingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle, SimTime duration)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        _movementManager.rotY(angle, duration);
        _isRotating = true;
        this.passivate();
        if (isInterrupted()) {
            //update the rotation and cancel the rotation in this class and in the SpatialMovementManager
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _isRotating = false;
            _movementManager.cancelRotatingDoneEvent();
            _movementManager.cleanUpRotation();
            //clean up the interrupt code if the rotation is canceled by cancelRotation()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelRotation)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#rotZ(double, desmoj.core.simulator.TimeSpan)
     */
    public void rotZ(double angle, TimeSpan duration) throws SuspendExecution {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        //		if(_isMovingConcurrent){
        //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
        //					"The object can't be rotated.",
        //					this.getName()+", MovableSpatialSimProcess.rotZ(double angle, SimTime duration)",
        //					"The object is already moving concurrently.",
        //					"Cancel the existed concurrent movement or wait until it stops before start a non-concurrent rotation.",
        //					this.presentTime());
        //			throw new IllegalMoveException(eMessage);
        //		}
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle, SimTime duration)",
                "The object is already moving.",
                "Cancel the existed movement or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating || _isRotatingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle, SimTime duration)",
                "The object is already rotating.",
                "Cancel the existed rotation or wait until it stops before start a non-concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        _movementManager.rotZ(angle, duration);
        _isRotating = true;
        this.passivate();
        if (isInterrupted()) {
            //update the rotation and cancel the rotation in this class and in the SpatialMovementManager
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _isRotating = false;
            _movementManager.cancelRotatingDoneEvent();
            _movementManager.cleanUpRotation();
            //clean up the interrupt code if the rotation is canceled by cancelRotation()
            if (InterruptCode.equals(this.getInterruptCode(), _interruptCodeCancelRotation)) {
                this.clearInterruptCode();
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#rotX(double)
     */
    @Override
    public void rotX(double angle) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The rotation can't be set.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle)",
                "The object is already rotating.",
                "The rotational property can't be set if the object is already rotating.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotX(double angle)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        super.rotX(angle);
        if (_frontSide != null) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
        }
        //		_movementManager.rotX(angle);
        //		_spatialData.rotX(angle);

    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#rotY(double)
     */
    @Override
    public void rotY(double angle) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess:rotY(double angle)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The rotation can't be set.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle)",
                "The object is already rotating.",
                "The rotational property can't be set if the object is already rotating.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotY(double angle)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        super.rotY(angle);
        if (_frontSide != null) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
        }
        //		_movementManager.rotY(angle);
        //		_spatialData.rotY(angle);

    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#rotZ(double)
     */
    @Override
    public void rotZ(double angle) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The rotation can't be set.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle)",
                "The object is already rotating.",
                "The rotational property can't be set if the object is already rotating.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZ(double angle)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        super.rotZ(angle);
        if (_frontSide != null) {
            //update the front side vector
            _spatialData.getMatrix().transform(_frontSide);
            _movementManager.setFrontSideVector(_frontSide);
        }
        //		_movementManager.rotZ(angle);
        //		_spatialData.rotZ(angle);

    }

    /**
     * Rotates the object about the x-axis using an animation. The difference to the rotX methods is that this method
     * won't passivate the MovableSpatialSimProcess after the execution so other actions like a movement can be
     * performed while the object is rotating.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    public void rotXConcurrent(double angle, TimeSpan duration) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotXConcurrent(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotXConcurrent(double angle, SimTime duration)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotXConcurrent(double angle, SimTime duration)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotXConcurrent(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotatingConcurrent) {
            _movementManager.cancelRotatingDoneEvent();
            //update and finish the current rotation and start the new one
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _movementManager.rotX(angle, duration);
        } else {
            _movementManager.rotX(angle, duration);
            _isRotatingConcurrent = true;
        }
    }

    /**
     * Rotates the object about the y-axis using an animation. The difference to the rotY methods is that this method
     * won't passivate the MovableSpatialSimProcess after the execution so other actions like a movement can be
     * performed while the object is rotating.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    public void rotYConcurrent(double angle, TimeSpan duration) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotYConcurrent(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotYConcurrent(double angle, SimTime duration)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotYConcurrent(double angle, SimTime duration)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotYConcurrent(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotatingConcurrent) {
            _movementManager.cancelRotatingDoneEvent();
            //update and finish the current rotation and start the new one
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _movementManager.rotY(angle, duration);
        } else {
            _movementManager.rotY(angle, duration);
            _isRotatingConcurrent = true;
        }
    }

    /**
     * Rotates the object about the z-axis using an animation. The difference to the rotZ methods is that this method
     * won't passivate the MovableSpatialSimProcess after the execution so other actions like a movement can be
     * performed while the object is rotating.
     *
     * @param angle    The start angle
     * @param duration The duration of the animation
     */
    public void rotZConcurrent(double angle, TimeSpan duration) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZConcurrent(double angle, SimTime duration)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotZConcurrent(double angle, SimTime duration)",
                "The object is already moving and passivated.",
                "Cancel the current movement first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotating) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The object can't be rotated.",
                this.getName() + ", MovableSpatialSimProcess.rotZConcurrent(double angle, SimTime duration)",
                "The object is already rotating and passivated.",
                "Cancel the current rotation first or wait until it's finished before start a concurrent rotation.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isFocusOn) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() + ", MovableSpatialSimProcess.rotZConcurrent(double angle, SimTime duration)",
                "The object is moving with focusOnTrack function. This function is controlling the orientation of the MovableSpatialEntity now.",
                "Finish the movement with focusOnTrack function before rotate this MovableSpatialObject.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isRotatingConcurrent) {
            _movementManager.cancelRotatingDoneEvent();
            //update and finish the current rotation and start the new one
            Rotation rotation = _movementManager.getCurrentRotationalDataDuringRotation();
            boolean[] axis = rotation.getAxis();
            if (axis[0] == true) {
                _spatialData.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                _spatialData.rotY(rotation.getAngle());
            } else if (axis[2] == true) {
                _spatialData.rotZ(rotation.getAngle());
            }

            //fire an interruptEvent
            Quat4d orientation = new Quat4d();
            _spatialData.getMatrix().get(orientation);
            _eventTransmitter.fireVisualEvent(
                new RotationInterruptEvent(this, this.getName(), orientation, this.presentTime()));

            if (_frontSide != null) {
                //update the front side vector
                _spatialData.getMatrix().transform(_frontSide);
                _movementManager.setFrontSideVector(_frontSide);
            }
            _movementManager.rotZ(angle, duration);
        } else {
            _movementManager.rotZ(angle, duration);
            _isRotatingConcurrent = true;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#sendToLocation(desmoj.extensions.space3D.SpatialObject)
     */
    public void sendToLocation(SpatialObject destination) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() + ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() + ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination)",
                "The object is already moving.",
                "The positional property can't be set if the object is already moving.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        } else {
            this.setPosition(destination.getPosX(), destination.getPosY(),
                destination.getPosZ());
            _currentLocation = destination;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#sendToLocationEntryPoint(desmoj.extensions.space3D.SpatialObject, java.lang.String)
     */
    public void sendToLocationEntryPoint(SpatialObject destination,
                                         String entryPoint) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() +
                    ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination, String entryPoint)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() +
                    ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination, String entryPoint)",
                "The object is already moving.",
                "The positional property can't be set if the object is already moving.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        } else {
            Length[] entryPointPos = destination.getEntryPointGlobal(entryPoint);
            if (entryPointPos == null) {
                this.sendWarning("This Object can't be sent to the given location",
                    this + " sendToLocationEntryPoint(SpatialObject" +
                        "destination, String entryPoint)",
                    "The given entry point can't be found at the" +
                        "destination object",
                    "Please recheck the model or the code");
            } else {
                this.setPosition(entryPointPos[0], entryPointPos[1], entryPointPos[2]);
                _currentLocation = destination;
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.MovableSpatialObject#sendToLocationExitPoint(desmoj.extensions.space3D.SpatialObject, java.lang.String)
     */
    public void sendToLocationExitPoint(SpatialObject destination,
                                        String exitPoint) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() +
                    ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination, String exitPoint)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This object can't be send to the destination.",
                this.getName() +
                    ", MovableSpatialSimProcess.sendToLocation(SpatialObject destination, String exitPoint)",
                "The object is already moving.",
                "The positional property can't be set if the object is already moving.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        } else {
            Length[] exitPointPos = destination.getExitPointGlobal(exitPoint);
            if (exitPointPos == null) {
                this.sendWarning("This Object can't be sent to the given location",
                    this + " sendToLocationExitPoint(SpatialObject" +
                        "destination, String exitPoint)",
                    "The given exit point can't be found at the" +
                        "destination object",
                    "Please recheck the model or the code");
            } else {
                this.setPosition(exitPointPos[0], exitPointPos[1], exitPointPos[2]);
                _currentLocation = destination;
            }
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialSimProcess#setPosition(desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength)
     */
    @Override
    public void setPosition(Length x, Length y, Length z) {
        if (_isAttached) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "This action can't be performed.",
                this.getName() +
                    ", MovableSpatialSimProcess.setPosition(ExtendedLength x, ExtendedLength y, ExtendedLength z)",
                "The object is already attached to another MovableSpatialObject.",
                "Detach the object before change its spatial properties freely.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        }
        if (_isMoving || _isMovingConcurrent) {
            ErrorMessage eMessage = new ErrorMessage(this.getModel(),
                "The position can't be set.",
                this.getName() +
                    ", MovableSpatialSimProcess.setPosition(ExtendedLength x, ExtendedLength y, ExtendedLength z)",
                "The object is already moving.",
                "The positional property can't be set if the object is already moving.",
                this.presentTime());
            throw new IllegalMoveException(eMessage);
        } else {
            super.setPosition(x, y, z);
        }
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.MovableSpatialObject#setFrontSideVector(org.scijava.vecmath.Vector3d)
    //	 */
    //	@Override
    //	public void setFrontSideVector(Vector3d frontSide){
    //		if(_isFocusOn){
    //			ErrorMessage eMessage = new ErrorMessage(this.getModel(),
    //					"The front side vector can't be set.",
    //					this.getName()+", MovableSpatialEntity.resetRotation()",
    //					"This vector is used by a movement now.",
    //					"The current movement must be finished before the new front side vector can be set.",
    //					this.presentTime());
    //			throw new IllegalMoveException(eMessage);
    //		}
    //		frontSide.normalize();
    //		_frontSide = frontSide;
    //		_movementManager.setFrontSideVector(_frontSide);
    //	}
}