package desmoj.extensions.visualization3d;

import org.scijava.java3d.AmbientLight;
import org.scijava.java3d.BoundingSphere;
import org.scijava.java3d.BranchGroup;
import org.scijava.java3d.Canvas3D;
import org.scijava.java3d.PointLight;
import org.scijava.java3d.Switch;
import org.scijava.java3d.Transform3D;
import org.scijava.java3d.TransformGroup;
import javax.swing.JFrame;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3d;
import org.scijava.vecmath.Point3f;
import org.scijava.vecmath.Quat4d;
import org.scijava.vecmath.Vector3d;
import org.scijava.vecmath.Vector3f;
import java.awt.GraphicsConfiguration;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.scijava.java3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import org.scijava.java3d.utils.universe.SimpleUniverse;
import desmoj.extensions.space3D.Movement;
import desmoj.extensions.space3D.Rotation;
import desmoj.extensions.space3D.SimpleTrack;
import desmoj.extensions.space3D.Track;
import desmoj.extensions.visualEvents.AttachEvent;
import desmoj.extensions.visualEvents.CreateVisibleObjectEvent;
import desmoj.extensions.visualEvents.DetachEvent;
import desmoj.extensions.visualEvents.MoveEvent;
import desmoj.extensions.visualEvents.MovementInterruptEvent;
import desmoj.extensions.visualEvents.RemoveEvent;
import desmoj.extensions.visualEvents.RotateEvent;
import desmoj.extensions.visualEvents.RotationInterruptEvent;
import desmoj.extensions.visualEvents.SetOrientationEvent;
import desmoj.extensions.visualEvents.SetPositionEvent;
import desmoj.extensions.visualEvents.SetVisibleEvent;
import desmoj.extensions.visualEvents.VisualEvent;
import desmoj.extensions.visualEvents.VisualEventListener;

/**
 * This Module visualize the 3D-spatial objects using Java3D.
 *
 * @author Fred Sun
 */
public class SpatialVis3DModule implements VisualModule {

    protected Canvas3D _canvas3D = null;
    //the name of this VisualModule
    private String _name;
    //a window to display the visualization
    private JFrame _frame;
    //the simpleUniverse for the virtual objects
    private SimpleUniverse _simpleUniverse;
    //the switch group to handle the visible groups
    private Switch _rootSwitch;
    //contains models with undefined group
    private BranchGroup _defaultGroup;
    //the model manager
    private ModelManager _modelManager;
    //the clock of the VisualizationControler
    private VisualizationClock _clock;
    //a list to hold the customized listeners
    private ArrayList<VisualEventListener> _listenerList = null;
    //a map holds the DynamicObjects
    private HashMap<String, DynamicObject> _dynamicObjectMap;
    //a map holds the StaticObjects
    private HashMap<String, StaticObject> _staticObjectMap;
    //where this module is registered to
    private VisualizationControl _visCon = null;

    /**
     * Constructs a SpatialVix3DModule and load the decoration models into the scene.
     *
     * @param name     the name of this module.
     * @param filePath The file path to the XML-file, where the model data is specified.
     */
    public SpatialVis3DModule(final String name, String filePath) {
        this(name, filePath, true);
    }

    public SpatialVis3DModule(final String name, String filePath, boolean createFrame) {
        init(name, filePath);
        if (createFrame) {
            createFrame(name);
        }
    }

    public void createFrame(final String name) {
        //create and setup frame
        _frame = new JFrame("SpatialVis3DModule: " + _name);
        _frame.setSize(800, 600);
        _frame.add("Center", _canvas3D);
        _frame.addWindowListener(new WindowAdapter() {

            public void windowClosing(WindowEvent e) {
                _simpleUniverse.cleanup();
                _frame.removeWindowListener(this);
                _frame.dispose();
                if (_visCon != null) {
                    _visCon.removeModule(name);
                }
            }
        });
        _frame.setVisible(true);
    }

    protected void init(final String name, String filePath) {
        //set the name of this VisualModule
        if (name == null) {
            throw new IllegalArgumentException("The name of the SpatialVis3DModule" +
                " must be specified.");
        }
        _name = name;

        //initialize clock
        _clock = VisualizationControl.getClock();

        //get the preferred graphicsConfiguration
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        //create a canvac3d
        _canvas3D = new Canvas3D(config);

        //create the simpleUniverse
        _simpleUniverse = new SimpleUniverse(_canvas3D);
        _simpleUniverse.getViewingPlatform().setNominalViewingTransform();
        _simpleUniverse.getViewer().getView().setBackClipDistance(100);

        //a world bounding sphere within light and camera control works
        BoundingSphere worldBounds = new BoundingSphere(new Point3d(), 100);

        //set camera control
        KeyNavigatorBehavior cameraControl =
            new KeyNavigatorBehavior(_simpleUniverse.getViewingPlatform().getViewPlatformTransform());
        cameraControl.setSchedulingBounds(worldBounds);

        //set the camera position
        TransformGroup cameraGroup = _simpleUniverse.getViewingPlatform().getViewPlatformTransform();
        Transform3D cameraTransform = new Transform3D();
        cameraTransform.setTranslation(new Vector3f(0f, 1f, 3f));
        cameraGroup.setTransform(cameraTransform);

        //initialize the maps for the active objects
        _dynamicObjectMap = new HashMap<String, DynamicObject>();
        _staticObjectMap = new HashMap<String, StaticObject>();

        //initiate the root switch
        _rootSwitch = new Switch();
        _rootSwitch.setCapability(Switch.ALLOW_SWITCH_WRITE);
        _rootSwitch.setCapability(Switch.ALLOW_CHILDREN_EXTEND);
        _rootSwitch.setWhichChild(Switch.CHILD_ALL);

        //initialize the model manager
        _modelManager = new ModelManager(filePath);

        //load decorations
        _rootSwitch.addChild(_modelManager.loadDecoration());

        //initialize the root of the scene graph
        BranchGroup rootBranch = new BranchGroup();

        //initialize the default group branch
        _defaultGroup = new BranchGroup();
        _defaultGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
        _defaultGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);

        //LIGHT
        PointLight plight1 = new PointLight();
        //        PointLight plight2 = new PointLight();
        //        PointLight plight3 = new PointLight();
        //        PointLight plight4 = new PointLight();
        plight1.setPosition(25, 100, 25);
        //        plight2.setPosition(-50, 100, 50);
        //        plight3.setPosition(50, 100, -50);
        //        plight4.setPosition(-50, 100, -50);
        plight1.setInfluencingBounds(worldBounds);
        //        plight2.setInfluencingBounds(lightBounds);
        //        plight3.setInfluencingBounds(lightBounds);
        //        plight4.setInfluencingBounds(worldBounds);
        plight1.setEnable(true);
        //        plight2.setEnable(true);
        //        plight3.setEnable(true);
        //        plight4.setEnable(true);
        rootBranch.addChild(plight1);
        //        rootBranch.addChild(plight2);
        //        rootBranch.addChild(plight3);
        //        rootBranch.addChild(plight4);
        AmbientLight al = new AmbientLight(new Color3f(1f, 1f, 1f));
        al.setInfluencingBounds(worldBounds);
        al.setEnable(true);
        rootBranch.addChild(al);

        //TEXT and BILLBOARD TEST
        //		Text2D text = new Text2D("test", new Color3f(1f,0f,0f), Font.DIALOG, 100, Font.PLAIN);
        //		TransformGroup textTG = new TransformGroup();
        //		textTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        //		Billboard textBoard = new Billboard(textTG);
        //		textBoard.setSchedulingBounds(worldBounds);
        //		textTG.addChild(text);
        //		textTG.addChild(textBoard);
        //		rootBranch.addChild(textTG);

        //attach the root nodes together
        rootBranch.addChild(_rootSwitch);
        _rootSwitch.addChild(_defaultGroup);
        rootBranch.addChild(cameraControl);
        rootBranch.compile();
        _simpleUniverse.addBranchGraph(rootBranch);


    }

    /* (non-Javadoc)
     * @see desmoj.extensions.visualization3d.VisualModule#getModuleName()
     */
    public String getModuleName() {
        return _name;
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.visualization3d.VisualModule#handleEvent(desmoj.extensions.space3D.VisualEvent)
     */
    public void handleEvent(VisualEvent event) {
        if (event instanceof CreateVisibleObjectEvent) {
            this.handleCreateSpatialObjectEvent((CreateVisibleObjectEvent) event);
        } else if (event instanceof RotateEvent) {
            this.handleRotateEvent((RotateEvent) event);
        } else if (event instanceof MoveEvent) {
            this.handleMoveEvent((MoveEvent) event);
        } else if (event instanceof SetOrientationEvent) {
            this.handleSetOrientationEvent((SetOrientationEvent) event);
        } else if (event instanceof SetPositionEvent) {
            this.handleSetPositionEvent((SetPositionEvent) event);
        } else if (event instanceof AttachEvent) {
            this.handleAttachEvent((AttachEvent) event);
        } else if (event instanceof DetachEvent) {
            this.handleDetachEvent((DetachEvent) event);
        } else if (event instanceof MovementInterruptEvent) {
            this.handleMovementInterruptEvent((MovementInterruptEvent) event);
        } else if (event instanceof RotationInterruptEvent) {
            this.handleRotationInterruptEvent((RotationInterruptEvent) event);
        } else if (event instanceof RemoveEvent) {
            this.handleRemoveEvent((RemoveEvent) event);
        } else if (event instanceof SetVisibleEvent) {
            this.handleSetVisibleEvent((SetVisibleEvent) event);
        }
        if (_listenerList != null) {
            int numListeners = _listenerList.size();
            for (int i = 0; i < numListeners; i++) {
                if (event.getClass().isInstance(_listenerList.get(i))) {
                    _listenerList.get(i).visualEventReceived(event);
                }
            }
        }
    }

    /**
     * Sets the visibility of the
     *
     * @param event
     */
    private void handleSetVisibleEvent(SetVisibleEvent event) {
        String name = event.getTargetName();
        boolean visible = event.getVisible();

        if (_dynamicObjectMap.containsKey(name)) {
            DynamicObject target = _dynamicObjectMap.get(name);
            BranchGroup detachBranchTarget = target.getDetachBranch();

            //add or remove the branch into or from the scene graph
            if (visible && detachBranchTarget.getParent() == null) {
                //checks where to attach
                if (target.isAttached()) {
                    target.getHostObject().getOrientationTransform().addChild(detachBranchTarget);
                } else {
                    _defaultGroup.addChild(detachBranchTarget);
                }
            } else if (!visible && detachBranchTarget.getParent() != null) {
                detachBranchTarget.detach();
            }
        } else if (_staticObjectMap.containsKey(name)) {
            BranchGroup detachBranchTarget = _staticObjectMap.get(name).getDetachBranch();

            //add or remove the branch into or from the scene graph
            if (visible && detachBranchTarget.getParent() == null) {
                _defaultGroup.addChild(detachBranchTarget);
            } else if (!visible && detachBranchTarget.getParent() != null) {
                detachBranchTarget.detach();
            }
        }
    }

    /**
     * Remove the dynamic/static object from the scene graph and the map.
     *
     * @param event The RemoveEvent
     */
    private void handleRemoveEvent(RemoveEvent event) {

        String name = event.getTargetName();

        if (_dynamicObjectMap.containsKey(name)) {
            //detach it from the scene graph
            _dynamicObjectMap.get(name).getDetachBranch().detach();
            //remove it from the map
            _dynamicObjectMap.remove(name);
        } else if (_staticObjectMap.containsKey(name)) {
            //detach it from the scene graph
            _staticObjectMap.get(name).getDetachBranch().detach();
            //remove it from the map
            _staticObjectMap.remove(name);
        }
    }

    /**
     * Handles the RotationInterruptEvent.
     *
     * @param event The RotationInterruptEvent.
     */
    private void handleRotationInterruptEvent(RotationInterruptEvent event) {

        String name = event.getTargetName();

        //check whether the object to be move exists
        if (!_dynamicObjectMap.containsKey(name)) {
            throw new RuntimeException("The visible object which should attach " +
                "can't be found.");
        }

        DynamicObject object = _dynamicObjectMap.get(name);

        //stop the rotation
        object.getRotateInterpolator().stop();

        //reset the orientation
        Transform3D rotationTrans = new Transform3D();
        rotationTrans.set(event.getStoppedAtOrientation());
        object.getOrientationTransform().setTransform(rotationTrans);
    }

    /**
     * Handles the MovementInterruptEvent.
     *
     * @param event The MovementInterruptEvent.
     */
    private void handleMovementInterruptEvent(MovementInterruptEvent event) {

        String name = event.getTargetName();

        //check whether the object to be move exists
        if (!_dynamicObjectMap.containsKey(name)) {
            throw new RuntimeException("The visible object which should attach " +
                "can't be found.");
        }

        DynamicObject object = _dynamicObjectMap.get(name);

        //stop the movement
        object.getMoveInterpolator().stop();

        //reset the position
        double[] newPosition = event.getStoppedAtPosition();
        Vector3d newPosVector = new Vector3d(newPosition[0], newPosition[1], newPosition[2]);
        Transform3D newPosTransform = new Transform3D();
        newPosTransform.setTranslation(newPosVector);
        object.getPositionTransform().setTransform(newPosTransform);

    }

    /**
     * Handles the detach event.
     *
     * @param event The detach event.
     */
    private void handleDetachEvent(DetachEvent event) {

        String name = event.getTargetName();

        //check whether the object to be move exists
        if (!_dynamicObjectMap.containsKey(name)) {
            throw new RuntimeException("The visible object which should attach " +
                "can't be found.");
        }

        DynamicObject detachObject = _dynamicObjectMap.get(name);

        if (detachObject.isAttached()) {
            detachObject.detach(_defaultGroup);
        }
    }

    /**
     * Handles the attach event.
     *
     * @param event The attach event
     */
    private void handleAttachEvent(AttachEvent event) {

        String attacherName = event.getTargetName();
        String hostName = event.getHostName();

        //check whether the object to be move exists
        if (!_dynamicObjectMap.containsKey(attacherName)) {
            throw new RuntimeException("The visible object which should attach " +
                "can't be found.");
        }

        if (!_dynamicObjectMap.containsKey(hostName)) {
            throw new RuntimeException("The visible object to be attached " +
                "can't be found.");
        }

        DynamicObject attacher = _dynamicObjectMap.get(attacherName);
        DynamicObject host = _dynamicObjectMap.get(hostName);

        if (!attacher.isAttached()) {
            attacher.attach(host);
        }
    }


    /**
     * Sets the object to the given position.
     *
     * @param event The event for setting new position.
     */
    private void handleSetPositionEvent(SetPositionEvent event) {
        // read out the event
        String name = event.getTargetName();

        //check whether the object to be move exists
        if (_dynamicObjectMap.containsKey(name)) {
            DynamicObject object = _dynamicObjectMap.get(name);
            TransformGroup positionTransGroup = object.getPositionTransform();
            Transform3D positionTransform = new Transform3D();
            positionTransform.setTranslation(new Vector3d(event.getX(), event.getY(), event.getZ()));
            MovementPathInterpolator mInterpolator = object.getMoveInterpolator();
            if (mInterpolator.getEnable()) {
                mInterpolator.stop();
            }
            positionTransGroup.setTransform(positionTransform);
        } else if (_staticObjectMap.containsKey(name)) {
            StaticObject object = _staticObjectMap.get(name);
            TransformGroup transGroup = object.getTransform();
            Transform3D positionTransform = new Transform3D();
            positionTransform.setTranslation(new Vector3d(event.getX(), event.getY(), event.getZ()));
            transGroup.setTransform(positionTransform);
        }


    }

    /**
     * Sets the object to the given orientation.
     *
     * @param event The event to set the orientation
     */
    private void handleSetOrientationEvent(SetOrientationEvent event) {
        // read out the event
        String name = event.getTargetName();
        Quat4d orientation = event.getOrientation();

        //check where the object to be move exists
        if (_dynamicObjectMap.containsKey(name)) {
            DynamicObject object = _dynamicObjectMap.get(name);
            TransformGroup orientationTransGroup = object.getOrientationTransform();
            Transform3D orientationTransform = new Transform3D();
            orientationTransform.set(orientation);
            RotationSwitchInterpolator rInterpolator = object.getRotateInterpolator();
            if (rInterpolator.getEnable()) {
                rInterpolator.stop();
            }
            orientationTransGroup.setTransform(orientationTransform);
        } else if (_staticObjectMap.containsKey(name)) {
            StaticObject object = _staticObjectMap.get(name);
            TransformGroup transGroup = object.getTransform();
            Transform3D orientationTransform = new Transform3D();
            orientationTransform.set(orientation);
            transGroup.setTransform(orientationTransform);
        }
    }

    /**
     * Moves the associated object.
     *
     * @param event The move event.
     */
    private void handleMoveEvent(MoveEvent event) {
        //read out the event
        String name = event.getTargetName();
        Movement movement = event.getMovement();

        //check whether the object to be move exists
        if (!_dynamicObjectMap.containsKey(name)) {
            throw new RuntimeException("The associated visible object" +
                "can't be found.");
        }

        Track track = movement.getTrack();

        if (track instanceof SimpleTrack) {
            this.moveSimpleTrack(_dynamicObjectMap.get(name), (SimpleTrack) track,
                movement, event.isFocusedOnTrack(), event.getFrontSideVector());
        }
    }

    /**
     * Rotate the associated object.
     *
     * @param event The rotate Event.
     */
    private void handleRotateEvent(RotateEvent event) {
        //read out the event
        String name = event.getTargetName();
        Rotation rotation = event.getRotation();
        boolean[] axis = rotation.getAxis();
        double duration = rotation.getDuration();
        TransformGroup orientationTransformGroup = null;

        //check whether the object to be rotate exists
        if (_dynamicObjectMap.containsKey(name)) {
            orientationTransformGroup = _dynamicObjectMap.get(name).getOrientationTransform();
        } else if (_staticObjectMap.containsKey(name)) {
            orientationTransformGroup = _staticObjectMap.get(name).getTransform();
        } else {
            throw new RuntimeException("The associated visible object" +
                "can't be found.");
        }

        Transform3D currentTransform = new Transform3D();
        orientationTransformGroup.getTransform(currentTransform);

        if (duration <= 0.) {
            //rotate it without animation
            Transform3D rotTransform = new Transform3D();
            if (axis[0] == true) {
                rotTransform.rotX(rotation.getAngle());
            } else if (axis[1] == true) {
                rotTransform.rotY(rotation.getAngle());
            } else {
                rotTransform.rotZ(rotation.getAngle());
            }
            currentTransform.mul(rotTransform);
            orientationTransformGroup.setTransform(currentTransform);
        } else {
            //rotate it with the interpolator
            RotationSwitchInterpolator rInterpolator = _dynamicObjectMap.get(name).getRotateInterpolator();
            VisualizationClockAlpha rotationAlpha = new VisualizationClockAlpha((long) (duration * 1000));

            Transform3D axisTransform = new Transform3D();
            //set the rotation axis.
            //the rotation is about the y-axis, so nothing have to be changed for this case
            if (axis[0] == true) {
                axisTransform.rotZ(-Math.PI / 2);
            } else if (axis[2] == true) {
                axisTransform.rotX(Math.PI / 2);
            }
            //start the rotation
            rInterpolator.start(rotationAlpha, (float) rotation.getAngle(), axisTransform, currentTransform);
        }
    }

    /**
     * Creates a Dynamic/StaticObject according to the event. Link the model into the scene.
     *
     * @param event The CreateVisibleObjectEvent which contains the details about the new object to be created.
     */
    private void handleCreateSpatialObjectEvent(CreateVisibleObjectEvent event) {
        //get the basic informations
        String name = event.getTargetName();
        String type = event.getVisualModel();
        boolean movable = event.isMovable();

        //ask the model manager about the 3d-model
        BranchGroup modelBranch = _modelManager.getModel(type);

        //create a Dynamic/StaticObject according to the event
        //and link it into the scene
        if (movable) {
            DynamicObject createdObject = new DynamicObject(name, type,
                modelBranch, _clock);
            _dynamicObjectMap.put(name, createdObject);
            _defaultGroup.addChild(createdObject.getDetachBranch());
        } else {
            StaticObject createdObject = new StaticObject(name, type,
                modelBranch);
            _staticObjectMap.put(name, createdObject);
            _defaultGroup.addChild(createdObject.getDetachBranch());
        }
    }

    /**
     * Performs the movement on a SimpleTrack
     *
     * @param activeObject    The object to be moved.
     * @param track           The SimpleTrack to be moved on.
     * @param movement        The movement data.
     * @param focusOnTrack    The flag indicates whether the object should facing to the moving direction while moving.
     * @param frontSideVector The Vector which shows the the front side of the object.
     */
    private void moveSimpleTrack(DynamicObject activeObject, SimpleTrack track,
                                 Movement movement, boolean focusOnTrack, Vector3d frontSideVector) {

        //initialize Alpha
        MovementPathInterpolator mInterpolator = activeObject.getMoveInterpolator();
        long duration = movement.getTotalDuration().getTimeRounded(TimeUnit.MILLISECONDS);
        VisualizationClockAlpha alpha = new VisualizationClockAlpha(duration);

        //initialize duration knots
        float[] durationKnots = new float[3];
        durationKnots[0] = movement.getAccDuration().getTimeRounded(TimeUnit.SECONDS);
        durationKnots[1] = movement.getMaxSpeedDuration().getTimeRounded(TimeUnit.SECONDS);
        durationKnots[2] = movement.getDecDuration().getTimeRounded(TimeUnit.SECONDS);

        //initialize speed knots
        float[] speedKnots = new float[3];
        speedKnots[0] = (float) movement.getInitialSpeed();
        speedKnots[1] = (float) movement.getMaxSpeed();
        speedKnots[2] = (float) movement.getEndSpeed();

        //initialize position knots
        int numWayPoints = 0;
        //all the way points
        ArrayList<double[]> wayPoints = track.getWayPoints();
        if (wayPoints != null) {
            numWayPoints = wayPoints.size();
        }

        Point3f[] positionKnots = new Point3f[numWayPoints + 2];
        double[] position = track.getStartPositionValue();
        //the start position
        positionKnots[0] = new Point3f((float) position[0], (float) position[1],
            (float) position[2]);

        //the position of the way points
        for (int i = 0; i < numWayPoints; i++) {
            position = wayPoints.get(i);
            positionKnots[i + 1] = new Point3f((float) position[0],
                (float) position[1], (float) position[2]);
        }
        //the destination position
        position = track.getDestinationValue();
        positionKnots[numWayPoints + 1] = new Point3f((float) position[0],
            (float) position[1], (float) position[2]);

        if (focusOnTrack) {
            mInterpolator.startWithFocusOnTrack(alpha, durationKnots, speedKnots, positionKnots, frontSideVector);
        } else {
            mInterpolator.start(alpha, durationKnots, speedKnots, positionKnots);
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.visualization3d.VisualModule#registerVisualizationControl(desmoj.extensions.visualization3d.VisualizationControl)
     */
    public void setVisualizationControl(VisualizationControl visCon) {
        _visCon = visCon;
    }

    /**
     * Registers a customized VisualEventListener to this module.
     *
     * @param listener The listener to be registered.
     */
    public void registerNewVisualEventListener(VisualEventListener listener) {
        if (_listenerList == null) {
            _listenerList = new ArrayList<VisualEventListener>();
        }
        _listenerList.add(listener);
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.visualization3d.VisualModule#removeVisualizationControl()
     */
    public void removeVisualizationControl() {
        _visCon = null;
    }

}
