package desmoj.extensions.visualization3d;

import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.TransformGroup;

/**
 * This class contains all elements needed in a scene graph to describe a static object (i.e. a not movable
 * SpatialObject) in a scene.
 * <p>
 * A StaticObject consists of a 3D-model which is contained in a BranchGroup, a TransformGroup who's responsible for the
 * transformation of the object and a BranchGroup which is the detaching point for this object.
 *
 * @author Fred Sun
 */
public class StaticObject {

    //the name of this object
    private final String _name;

    //the type of this object
    private final String _type;

    //the TransformGroup who's responsible for the transformation
    private final TransformGroup _transform;

    //the BranchGroup which contains the 3D-Model
    private final BranchGroup _modelBranch;

    //a BranchGroup for detaching this object
    private final BranchGroup _detachBranch;

    /**
     * Constructs a StaticObject.
     *
     * @param name        The name of this object.
     * @param type        The type of this object.
     * @param modelBranch The 3D-model BranchGroup.
     */
    StaticObject(String name, String type, BranchGroup modelBranch) {
        assert (name != null) : "The name of the object must be specified.";
        assert (type != null) : "The type of the object must be specified.";

        _name = name;
        _type = type;
        _modelBranch = modelBranch;
        _transform = new TransformGroup();
        _transform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _detachBranch = new BranchGroup();
        _detachBranch.setCapability(BranchGroup.ALLOW_DETACH);
        if (modelBranch != null) {
            //initialize the node groups
            _modelBranch.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

            //link the nodes together
            _transform.addChild(modelBranch);
        }
        _detachBranch.addChild(_transform);
        _detachBranch.compile();
    }

    /**
     * Gets the name of the StaticObject
     *
     * @return The name of the StaticObject
     */
    String getName() {
        return _name;
    }

    /**
     * Gets the type of the StaticObject.
     *
     * @return The type of the StaticObject
     */
    String getType() {
        return _type;
    }

    /**
     * Gets the TransformGroup which is responsible for the transformation of this object.
     *
     * @return the TransformGroup associated to this StaticObject.
     */
    TransformGroup getTransform() {
        return _transform;
    }

    /**
     * Gets the BranchGroup which contains the 3D-model.
     *
     * @return The BranchGroup which contains the 3D-model.
     */
    BranchGroup getModelBranch() {
        return _modelBranch;
    }

    /**
     * Gets the BranchGroup for detaching this obejct from scene graph.
     *
     * @return The detach BranchGroup
     */
    BranchGroup getDetachBranch() {
        return _detachBranch;
    }
}
