package desmoj.extensions.space3D;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.Model;

/**
 * The MovingDoneEvent class represents a internal event which notifies a MovableSpatialObject the end of its movement.
 * And it should only be used by a MovableSpatialObject. If some other entities call the constructor a DESMOJException
 * will be thrown.
 *
 * @author Fred Sun
 */
public class MovingDoneEvent extends ExternalEvent {

    private final MovableSpatialObject _targetObject;

    /**
     * Constructs a MovingDoneEvent and checks whether the caller is a instance of the MovableSpatialObject. If not, a
     * DESMOJException will be thrown.
     *
     * @param owner        The model this event is associated to
     * @param name         The name of this event
     * @param showInTrace  Flag for showing event in trace-files. Set it to true if event should show up in trace. Set
     *                     it to false if event should not be shown in trace.
     * @param targetObject A reference to the object which should be the target of this event
     */
    public MovingDoneEvent(Model owner, String name, boolean showInTrace, MovableSpatialObject targetObject) {
        super(owner, name, showInTrace);
        //		if(!(targetObject instanceof MovableSpatialObject)){
        //			throw new DESMOJException(new ErrorMessage(this.getModel(),
        //					"The MovingDoneEvent is called by a non-MovableSpatialObject",
        //					this.toString(),
        //					"This MovingDoneEvent is called by an entity which isn't a MovableSpatialObject.",
        //					"A MovingDoneEvent can only be called by a MovableSpatialObject. Please check your code.",
        //					this.presentTime()));
        //		}
        _targetObject = targetObject;
    }

    //	/* (non-Javadoc)
    //	 * @see desmoj.core.simulator.Event#eventRoutine(desmoj.core.simulator.Entity)
    //	 */
    //	@Override
    //	public void eventRoutine(E who) {
    //		((MovableSpatialObject) who).notifyMovingDone();
    //	}

    @Override
    public void eventRoutine() throws SuspendExecution {
        _targetObject.notifyMovingDone();
    }

}
