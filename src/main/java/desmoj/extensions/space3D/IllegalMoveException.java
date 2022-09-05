package desmoj.extensions.space3D;

import desmoj.core.exception.DESMOJException;
import desmoj.core.report.ErrorMessage;

/**
 * An IllegalMoveException can be thrown from a move method. The reason could be an insufficient length of the track
 * which is shorter than the breaking distance or a maximum speed limit which should be exceeded to reach the
 * destination in time or other reasons why a moving call can't be performed.
 *
 * @author Fred Sun
 */
public class IllegalMoveException extends DESMOJException {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = 3681988041606366798L;

    /**
     * Constructs an IllegalMoveException.
     *
     * @param message
     */
    public IllegalMoveException(ErrorMessage message) {
        super(message);
    }

}
