package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;
import org.scijava.vecmath.Point3d;
import org.scijava.vecmath.Quat4d;
import java.util.HashMap;
import java.util.Set;

import desmoj.core.simulator.Entity;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.Queue;
import desmoj.extensions.visualEvents.CreateVisibleObjectEvent;
import desmoj.extensions.visualEvents.RemoveEvent;
import desmoj.extensions.visualEvents.RotateEvent;
import desmoj.extensions.visualEvents.SetOrientationEvent;
import desmoj.extensions.visualEvents.SetPositionEvent;
import desmoj.extensions.visualEvents.SetVisibleEvent;
import desmoj.extensions.visualEvents.VisualEventTransmitter;

/**
 * This class extends the <code>desmoj.core.simulator.Queue&lt;E&gt;</code> with spatial functionalities. Every
 * SpatialQueue is associated with a spatial position and orientation.
 *
 * @author Fred Sun
 */
public class SpatialQueue<E extends Entity> extends Queue<E> implements SpatialObject,
    VisibleObject {

    //The entry points of this object
    private final HashMap<String, Point3d> _entryPoints;

    //Contains the spatial information of this queue
    private final SpatialData _spatialData = new SpatialData();

    //the exit points of this object
    private final HashMap<String, Point3d> _exitPoints;

    //a transmitter for sending the SpatialEvents
    private final VisualEventTransmitter _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();

    //The visual model attribute belongs to the SpatialObject interface.
    private String _visualModel;

    //shows whether this object is visible. true by default
    private boolean _isVisible;

    /**
     * Constructs a SpatialQueue at the specific position. Beside of the spatial extension the behaviors and the
     * functionalities of the SpatialQueue remain the same as the Queue. For more information about the Queue, and the
     * constructor, please see
     * <code>desmoj.core.simulator.Queue&lt;E&gt;</code>
     *
     * @param owner          The model this queue is associated to.
     * @param name           The queue's name.
     * @param visualModel    The visual model attribute belongs to the SpatialObject interface.
     * @param showInReport   Flag if queue should produce a report.
     * @param showInTrace    Flag for queue to produce trace messages.
     * @param startPositionX The x-coordinate of the start position.
     * @param startPositionY The y-coordinate of the start position.
     * @param startPositionZ The z-coordinate of the start position.
     */
    public SpatialQueue(Model owner, String name, String visualModel,
                        boolean showInReport, boolean showInTrace,
                        Length startPositionX, Length startPositionY,
                        Length startPositionZ) {
        super(owner, name, showInReport, showInTrace);

        _eventTransmitter.fireVisualEvent(
            new CreateVisibleObjectEvent(this, this.getName(), this._visualModel, false, this.presentTime()));
        if (startPositionX != null && startPositionY != null && startPositionZ != null) {
            this.setPosition(startPositionX, startPositionY, startPositionZ);
        } else {
            this.sendWarning("The start position isn't specified enough.",
                this + ": Constructor(Model, String, boolean, boolean," +
                    " ExtendedLength, ExtendedLength, ExtendedLength)",
                "One or more ExtendedLength is null",
                "Please recheck the code");
        }

        _entryPoints = new HashMap<String, Point3d>(1, 0.75f);
        _exitPoints = new HashMap<String, Point3d>(1, 0.75f);
        _visualModel = visualModel;
    }

    /**
     * Constructs a SpatialQueue at the specific position. Beside of the spatial extension the behaviors and the
     * functionalities of the SpatialQueue remain the same as the Queue. For more information about the Queue, and the
     * constructor, please see
     * <code>desmoj.core.simulator.Queue&lt;E&gt;</code>
     *
     * @param owner          The model this queue is associated to.
     * @param name           The queue's name.
     * @param visualModel    The visual model attribute belongs to the SpatialObject interface.
     * @param sortOrder      determines the sort order of the underlying queue implementation. Choose a constant from
     *                       QueueBased like QueueBased.FIFO or QueueBased.LIFO or ...
     * @param qCapacity      The capacity of the Queue, that is how many entities can be enqueued. Zero (0) means
     *                       unlimited capacity.
     * @param showInReport   Flag if queue should produce a report.
     * @param showInTrace    Flag for queue to produce trace messages.
     * @param startPositionX The x-coordinate of the start position.
     * @param startPositionY The y-coordinate of the start position.
     * @param startPositionZ The z-coordinate of the start position.
     */
    public SpatialQueue(Model owner, String name, String visualModel,
                        int sortOrder, int qCapacity, boolean showInReport,
                        boolean showInTrace, Length startPositionX,
                        Length startPositionY, Length startPositionZ) {
        super(owner, name, sortOrder, qCapacity, showInReport, showInTrace);

        _eventTransmitter.fireVisualEvent(
            new CreateVisibleObjectEvent(this, this.getName(), this._visualModel, false, this.presentTime()));
        if (startPositionX != null && startPositionY != null && startPositionZ != null) {
            this.setPosition(startPositionX, startPositionY, startPositionZ);
        } else {
            this.sendWarning("The start position isn't specified enough.",
                this + ": Constructor(Model, String, boolean, boolean," +
                    " int, int, ExtendedLength, ExtendedLength, ExtendedLength)",
                "One or more ExtendedLength is null",
                "Please recheck the code");
        }

        _entryPoints = new HashMap<String, Point3d>(1, 0.75f);
        _exitPoints = new HashMap<String, Point3d>(1, 0.75f);
        _visualModel = visualModel;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#addEntryPoint(java.lang.String, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength)
     */
    public void addEntryPoint(String name, Length xPos,
                              Length yPos, Length zPos) {
        double[] position = new double[3];
        position[0] = xPos.getValue();
        position[1] = yPos.getValue();
        position[2] = zPos.getValue();
        _entryPoints.put(name, new Point3d(position));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#addExitPoint(java.lang.String, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength)
     */
    public void addExitPoint(String name, Length xPos,
                             Length yPos, Length zPos) {
        double[] position = new double[3];
        position[0] = xPos.getValue();
        position[1] = yPos.getValue();
        position[2] = zPos.getValue();
        _exitPoints.put(name, new Point3d(position));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getAllEntryPointName()
     */
    public Set<String> getEntryPointNames() {
        return _entryPoints.keySet();
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getAllExitPointName()
     */
    public Set<String> getExitPointNames() {
        return _exitPoints.keySet();
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getEntryPoint(java.lang.String)
     */
    public Length[] getEntryPoint(String name) {
        Point3d point = _entryPoints.get(name);
        if (point == null) {
            return null;
        } else {
            Length[] result = new Length[3];

            result[0] = new Length(point.x);
            result[1] = new Length(point.y);
            result[2] = new Length(point.z);

            return result;
        }
    }


    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getEntryPointGlobal(java.lang.String)
     */
    public Length[] getEntryPointGlobal(String name) {
        Point3d point = new Point3d(_entryPoints.get(name));
        if (point == null) {
            return null;
        } else {
            //transform the local coordinates to the global coordinates
            _spatialData.getMatrix().transform(point);

            Length[] result = new Length[3];

            result[0] = new Length(point.x);
            result[1] = new Length(point.y);
            result[2] = new Length(point.z);

            return result;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getExitPoint(java.lang.String)
     */
    public Length[] getExitPoint(String name) {
        Point3d point = _exitPoints.get(name);
        if (point == null) {
            return null;
        } else {
            Length[] result = new Length[3];

            result[0] = new Length(point.x);
            result[1] = new Length(point.y);
            result[2] = new Length(point.z);

            return result;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getExitPointGlobal(java.lang.String)
     */
    public Length[] getExitPointGlobal(String name) {
        Point3d point = new Point3d(_exitPoints.get(name));
        if (point == null) {
            return null;
        } else {
            //transform the local coordinates to the global coordinates
            _spatialData.getMatrix().transform(point);

            Length[] result = new Length[3];

            result[0] = new Length(point.x);
            result[1] = new Length(point.y);
            result[2] = new Length(point.z);

            return result;
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getMatrix()
     */
    public Matrix4d getMatrix() {
        return _spatialData.getMatrix();
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getPosX()
     */
    public Length getPosX() {
        return new Length(_spatialData.getPosX());
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getPosY()
     */
    public Length getPosY() {
        return new Length(_spatialData.getPosY());
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#getPosZ()
     */
    public Length getPosZ() {
        return new Length(_spatialData.getPosZ());
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.VisibleObject#getType()
     */
    public String getVisualModel() {
        return _visualModel;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#isMovable()
     */
    public boolean isMovable() {
        return false;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.VisibleObject#isVisible()
     */
    public boolean isVisible() {
        return _isVisible;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.VisibleObject#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        _eventTransmitter.fireVisualEvent(new SetVisibleEvent(this, this.getName(), visible, this.presentTime()));
        _isVisible = visible;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.VisibleObject#removeVisible()
     */
    public void removeVisible() {
        _eventTransmitter.fireVisualEvent(new RemoveEvent(this, this.getName(), this.presentTime()));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#resetRotation()
     */
    public void resetRotation() {
        _spatialData.resetRotation();
        _eventTransmitter.fireVisualEvent(
            new SetOrientationEvent(this, this.getName(), new Quat4d(0., 0., 0., 1.), this.presentTime()));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#rotX(double)
     */
    public void rotX(double angle) {
        _spatialData.rotX(angle);
        boolean[] axis = {true, false, false};
        Rotation rotation = new Rotation(axis, angle, 0.0);
        _eventTransmitter.fireVisualEvent(new RotateEvent(this, this.getName(), rotation, this.presentTime()));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#rotY(double)
     */
    public void rotY(double angle) {
        _spatialData.rotY(angle);
        boolean[] axis = {false, true, false};
        Rotation rotation = new Rotation(axis, angle, 0.0);
        _eventTransmitter.fireVisualEvent(new RotateEvent(this, this.getName(), rotation, this.presentTime()));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#rotZ(double)
     */
    public void rotZ(double angle) {
        _spatialData.rotZ(angle);
        boolean[] axis = {false, false, true};
        Rotation rotation = new Rotation(axis, angle, 0.0);
        _eventTransmitter.fireVisualEvent(new RotateEvent(this, this.getName(), rotation, this.presentTime()));
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.SpatialObject#setPosition(desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength, desmoj.extensions.space3D.ExtendedLength)
     */
    public void setPosition(Length x, Length y, Length z) {
        assert (x != null && y != null && z != null) : "The coordinates must be specified.";
        double valX = x.getValue();
        double valY = y.getValue();
        double valZ = z.getValue();
        _spatialData.setPosition(valX, valY, valZ);
        _eventTransmitter.fireVisualEvent(
            new SetPositionEvent(this, this.getName(), valX, valY, valZ, this.presentTime()));
    }

}
