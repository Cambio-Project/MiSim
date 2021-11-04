package desmoj.extensions.visualization3d;

import org.scijava.java3d.BoundingSphere;
import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;
import org.scijava.vecmath.Point3d;
import org.scijava.vecmath.Quat4d;
import org.scijava.vecmath.Vector3d;

/**
 * This class contains all elements needed in a scene graph to describe a dynamic object (i.e. a MovableSpatialObject)
 * in a scene.
 * <p>
 * A DynamicObject contains several elements:
 * </br></br>
 * - a BranchGroup for attach/detach it to/from nodes</br> - a BranchGroup which contains the 3D-model</br> - a
 * TransformGroup which's responsible for the positioning</br> - a TransformGroup which's responsible for the
 * orientation</br> - a MovementPathInterpolator to move the object in time</br> - a Rotation to rotate the object in
 * time</br>
 *
 * @author Fred Sun
 */
public class DynamicObject {

    // the name of this DynamicObject
    private final String _name;

    //the type of this DynamicObject
    private final String _type;

    //the BranchGroup for detaching
    private final BranchGroup _detachBranch;

    //the BranchGroup which contains the 3D-model
    private final BranchGroup _modelBranch;

    //the TransformGroup for the translation
    private final TransformGroup _positionTransform;

    //the TransformGroup for the rotation
    private final TransformGroup _orientationTransform;

    //a Interpolator to move the object
    private final MovementPathInterpolator _moveInterpolator;

    //a Interpolator to rotate the object
    private final RotationSwitchInterpolator _rotateInterpolator;

    //the flag indicates whether the object is attached to another DynamicObject
    private boolean _isAttached;

    //the DynamicObject this object is attached to
    private DynamicObject _hostObject;

    /**
     * Constructs a DynamicObject.
     *
     * @param name        The name of this object.
     * @param type        The type of this object.
     * @param modelBranch The 3D-model BranchGroup.
     * @param clock       The VisualizatinoClock from the VisualizationControl object to initiate the interpolator's and
     *                    VisualizatinoClockAlpha.
     */
    DynamicObject(String name, String type, BranchGroup modelBranch,
                  VisualizationClock clock) {
        assert (name != null) : "The name of the object must be specified.";
        assert (type != null) : "The type of the object must be specified.";
        assert (modelBranch != null) : "The BranchGroup which contains the" +
            " 3D-model must be specified.";
        _name = name;
        _type = type;
        _modelBranch = modelBranch;
        _isAttached = false;

        //create the node groups
        _detachBranch = new BranchGroup();
        _detachBranch.setCapability(BranchGroup.ALLOW_DETACH);
        _positionTransform = new TransformGroup();
        _positionTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _orientationTransform = new TransformGroup();
        _orientationTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _orientationTransform.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
        _orientationTransform.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

        //set the position to far away so the object doesn't pop-up
        //in the middle at the creation
        Transform3D startPosTrans = new Transform3D();
        Vector3d startPos = new Vector3d(0f, -100f, 0f);
        startPosTrans.setTranslation(startPos);
        _positionTransform.setTransform(startPosTrans);

        //create interpolator
        BoundingSphere bound = new BoundingSphere(new Point3d(), 110);
        _moveInterpolator = new MovementPathInterpolator(_positionTransform, _orientationTransform);
        _moveInterpolator.setSchedulingBounds(bound);
        _rotateInterpolator = new RotationSwitchInterpolator(_orientationTransform);
        _rotateInterpolator.setSchedulingBounds(bound);

        //connect the node groups
        _detachBranch.addChild(_positionTransform);
        _positionTransform.addChild(_orientationTransform);
        _positionTransform.addChild(_moveInterpolator);
        _orientationTransform.addChild(_modelBranch);
        _orientationTransform.addChild(_rotateInterpolator);
        _detachBranch.compile();

    }

    /**
     * Attach this object to another DynamicObject.
     *
     * @param host The DynamicObject to attach to.
     */
    void attach(DynamicObject host) {

        _hostObject = host;
        //get the host spatial data into a transform3d
        TransformGroup hostPosGroup = _hostObject.getPositionTransform();
        TransformGroup hostOriGroup = _hostObject.getOrientationTransform();
        Transform3D hostTrans = new Transform3D();
        Transform3D hostOriTrans = new Transform3D();
        hostPosGroup.getTransform(hostTrans);
        hostOriGroup.getTransform(hostOriTrans);
        hostTrans.mul(hostOriTrans);

        //the spatial data of this object
        Transform3D myTrans = new Transform3D();
        Transform3D myOri = new Transform3D();
        _positionTransform.getTransform(myTrans);
        _orientationTransform.getTransform(myOri);
        myTrans.mul(myOri);

        //new transform = inverted host * this spatial data
        Transform3D newTrans = new Transform3D();
        newTrans.invert(hostTrans);
        newTrans.mul(myTrans);

        //detach this branch
        _detachBranch.detach();

        //reset data
        myOri.setIdentity();
        _orientationTransform.setTransform(myOri);
        _positionTransform.setTransform(newTrans);

        //add this object to the host object's branch
        hostOriGroup.addChild(_detachBranch);

        _isAttached = true;
    }

    /**
     * Detach the object from the host.
     *
     * @param nextRoot The root BranchGroup this object will be added to after the detaching.
     */
    void detach(BranchGroup nextRoot) {
        //get the host spatial data into a transform3d
        TransformGroup hostPosGroup = _hostObject.getPositionTransform();
        TransformGroup hostOriGroup = _hostObject.getOrientationTransform();
        Transform3D hostTrans = new Transform3D();
        Transform3D hostOriTrans = new Transform3D();
        hostPosGroup.getTransform(hostTrans);
        hostOriGroup.getTransform(hostOriTrans);
        hostTrans.mul(hostOriTrans);

        //get the spatial data of this object
        Transform3D myCurrentLocalTrans = new Transform3D();
        _positionTransform.getTransform(myCurrentLocalTrans);
        Transform3D myTrans = new Transform3D(hostTrans);
        myTrans.mul(myCurrentLocalTrans);

        //gets the positional and the rotational data
        Vector3d pos = new Vector3d();
        myTrans.get(pos);
        Quat4d rot = new Quat4d();
        myTrans.get(rot);

        //detach the object
        _detachBranch.detach();

        //sets new data
        Transform3D newTrans = new Transform3D();
        newTrans.setTranslation(pos);
        _positionTransform.setTransform(newTrans);

        newTrans.set(rot);
        _orientationTransform.setTransform(newTrans);

        //attach back to the root
        nextRoot.addChild(_detachBranch);

        _hostObject = null;
        _isAttached = false;
    }

    /**
     * Gets the name of this object.
     *
     * @return the name.
     */
    String getName() {
        return _name;
    }

    /**
     * Gets the type of this object.
     *
     * @return the type.
     */
    String getType() {
        return _type;
    }

    /**
     * Gets the detach BranchGroup of this object.
     *
     * @return the detach BranchGroup.
     */
    BranchGroup getDetachBranch() {
        return _detachBranch;
    }

    /**
     * Gets the BranchGroup contains the 3D-model.
     *
     * @return the the BranchGroup contains the 3D-model.
     */
    BranchGroup getModelBranch() {
        return _modelBranch;
    }

    /**
     * Gets the TransformGroup responsible for the position.
     *
     * @return the TransformGroup responsible for the transformation
     */
    TransformGroup getPositionTransform() {
        return _positionTransform;
    }

    /**
     * Gets the TransformGroup responsible for the orientation.
     *
     * @return the TransformGroup responsible for the orientation
     */
    TransformGroup getOrientationTransform() {
        return _orientationTransform;
    }

    /**
     * Gets the interpolator which is responsible for the translational movements.
     *
     * @return the the interpolator which is responsible for the translational movements
     */
    MovementPathInterpolator getMoveInterpolator() {
        return _moveInterpolator;
    }

    /**
     * Gets the interpolator which is responsible for the rotational movements.
     *
     * @return the interpolator which is responsible for the rotational movements
     */
    RotationSwitchInterpolator getRotateInterpolator() {
        return _rotateInterpolator;
    }

    /**
     * Returns whether this object is attached to another DynamicObject.
     *
     * @return whether this object is attached to another DynamicObject
     */
    boolean isAttached() {
        return _isAttached;
    }

    /**
     * If this object is attached, returns the host object.
     *
     * @return the host object this object is attached to. Null, if this object isn't attached to any object.
     */
    DynamicObject getHostObject() {
        return _hostObject;
    }

}
