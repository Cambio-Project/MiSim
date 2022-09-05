package desmoj.extensions.visualEvents;

import org.scijava.vecmath.Vector3d;

import desmoj.core.simulator.TimeInstant;
import desmoj.extensions.space3D.Movement;


/**
 * The MoveEvent indicates the event when a SpatialObject is be moving or a new position is set. It could be received by
 * visualizing instances or animation recording instances.
 *
 * @author Fred Sun
 */
public class MoveEvent extends VisualEvent {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = -6310947971183680228L;

    //The flag for the focus on track function
    private final boolean _focusOnTrack;

    //The movement which should be performed
    private final Movement _movement;

    //the frontSideVector for the case that the
    //focusOnTrack function is on
    private final Vector3d _fronSideVector;

    /**
     * Constructs a MoveEvent.
     *
     * @param source       The object on which the event initially occurred.
     * @param moverName    The name of the object who's moving.
     * @param movement     The movement which should be performed.
     * @param frontSideVec The front side vector if the focusOnTrack is on.
     * @param occuredTime  The TimeInstant when the event occurred.
     */
    public MoveEvent(Object source, String moverName,
                     Movement movement, boolean focusOnTrack, Vector3d frontSideVec, TimeInstant occurredTime) {
        super(source, moverName, occurredTime);
        assert (!(focusOnTrack && frontSideVec == null)) : "The front side vector" +
            " must be specified, if focusOnTrack is on.";
        _movement = movement;
        _focusOnTrack = focusOnTrack;
        _fronSideVector = frontSideVec;
    }

    /**
     * Gets the movement which should be performed.
     *
     * @return the movement.
     */
    public Movement getMovement() {
        return _movement;
    }

    /**
     * Shows whether the moving object should focus on the track while performing the movement.
     *
     * @return true, if it should focus on the track.
     */
    public boolean isFocusedOnTrack() {
        return _focusOnTrack;
    }

    /**
     * Gets the front side vector if exists.
     *
     * @return The front side vector.
     */
    public Vector3d getFrontSideVector() {
        return _fronSideVector;
    }

}
