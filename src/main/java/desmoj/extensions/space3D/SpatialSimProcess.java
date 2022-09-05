package desmoj.extensions.space3D;

import org.scijava.vecmath.Matrix4d;
import org.scijava.vecmath.Point3d;
import org.scijava.vecmath.Quat4d;
import java.util.HashMap;
import java.util.Set;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.SimProcess;
import desmoj.extensions.visualEvents.CreateVisibleObjectEvent;
import desmoj.extensions.visualEvents.RemoveEvent;
import desmoj.extensions.visualEvents.RotateEvent;
import desmoj.extensions.visualEvents.SetOrientationEvent;
import desmoj.extensions.visualEvents.SetPositionEvent;
import desmoj.extensions.visualEvents.SetVisibleEvent;
import desmoj.extensions.visualEvents.VisualEventTransmitter;

/**
 * This class represents the DESMO-J SimProcess which should be visualized but not movable in the 3D space. It
 * implements the SpatialObject interface to handle with the 3D spatial data.
 *
 * @author Fred Sun
 */
public abstract class SpatialSimProcess extends SimProcess implements SpatialObject,
    VisibleObject {

    //A class which contains the 3D data of the entity
    protected SpatialData _spatialData;
    //The entry points of this object
    private final HashMap<String, Point3d> _entryPoints;
    //The singleton VisualEventTransmitter
    private final VisualEventTransmitter _eventTransmitter;
    //the exit points of this object
    private final HashMap<String, Point3d> _exitPoints;
    // the visual model attribute belongs to the SpatialObject interface
    private final String _visualModel;

    //shows whether this object is visible. true by default
    private boolean _isVisible;

    /**
     * Constructs a SpatialSimProcess
     *
     * @param owner       The model this entity is associated to.
     * @param name        The name of the process.
     * @param visualModel The visual model attribute belongs to the SpatialObject interface.
     * @param showInTrace Flag for showing entity in trace-files. Set it to true if entity should show up in trace. Set
     *                    to false in entity should not be shown in trace.
     */
    public SpatialSimProcess(Model owner, String name, String visualModel,
                             boolean showInTrace) {
        super(owner, name, showInTrace);
        _spatialData = new SpatialData();
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
        _eventTransmitter.fireVisualEvent(
            new CreateVisibleObjectEvent(this, this.getName(), visualModel, this.isMovable(), this.presentTime()));
        _entryPoints = new HashMap<String, Point3d>(5, 0.9f);
        _exitPoints = new HashMap<String, Point3d>(5, 0.9f);
        _visualModel = visualModel;
        _isVisible = true;
    }

    /**
     * Constructs a SpatialSimProcess with specific start position.
     *
     * @param owner          The model this process is associated to.
     * @param name           The name of the process.
     * @param visualModel    The visual model attribute belongs to the SpatialObject interface.
     * @param showInTrace    Flag for showing entity in trace-files. Set it to true if entity should show up in trace.
     *                       Set to false in entity should not be shown in trace.
     * @param startPositionX The x start position in ExtendedLength.
     * @param startPositionY The y start position in ExtendedLength.
     * @param startPositionZ The z start position in ExtendedLength.
     */
    public SpatialSimProcess(Model owner, String name, String visualModel,
                             boolean showInTrace, Length startPositionX,
                             Length startPositionY, Length startPositionZ) {
        super(owner, name, showInTrace);
        _eventTransmitter = VisualEventTransmitter.getVisualEventTransmitter();
        if (startPositionX != null && startPositionY != null && startPositionZ != null) {
            //			double[][] spatialMatrix = new double[4][4];
            //			spatialMatrix[0][3] = startPositionX.getValue(6);
            //			spatialMatrix[1][3] = startPositionY.getValue(6);
            //			spatialMatrix[2][3] = startPositionZ.getValue(6);
            double[] spatialMatrix = new double[16];
            spatialMatrix[3] = startPositionX.getValue();
            spatialMatrix[7] = startPositionY.getValue();
            spatialMatrix[11] = startPositionZ.getValue();
            _spatialData = new SpatialData(spatialMatrix);
        } else {
            _spatialData = new SpatialData();
            this.sendWarning("The start position isn't specified enough.",
                this + ": Constructor(Model, String, boolean," +
                    " ExtendedLength, ExtendedLength, ExtendedLength)",
                "One or more ExtendedLength is null",
                "Please recheck the code");
        }
        _eventTransmitter.fireVisualEvent(
            new CreateVisibleObjectEvent(this, this.getName(), visualModel, this.isMovable(), this.presentTime()));
        _eventTransmitter.fireVisualEvent(
            new SetPositionEvent(this, this.getName(), _spatialData.getPosX(), _spatialData.getPosY(),
                _spatialData.getPosZ(), this.presentTime()));
        _entryPoints = new HashMap<String, Point3d>(5, 0.9f);
        _exitPoints = new HashMap<String, Point3d>(5, 0.9f);
        _visualModel = visualModel;
        _isVisible = true;
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
     * @see desmoj.extensions.space3D.SpatialObject#isMovable()
     */
    public boolean isMovable() {
        return this instanceof MovableSpatialObject;
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
     * @see desmoj.extensions.space3D.VisibleObject#getType()
     */
    public String getVisualModel() {
        return _visualModel;
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

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.visualization3d.SpatialObject#update(double[][])
    //	 */
    //	@Override
    //	public void update(double[] matrix) {
    //		_spatialData.setMatrix(matrix);
    //	}

    //	/* (non-Javadoc)
    //	 * @see desmoj.extensions.space3D.SpatialObject#update(org.scijava.vecmath.Matrix4d)
    //	 */
    //	@Override
    //	public void update(Matrix4d matrix){
    //		_spatialData.setMatrix(matrix);
    //	}

}
