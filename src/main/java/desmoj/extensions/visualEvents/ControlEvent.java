package desmoj.extensions.visualEvents;

import java.util.EventObject;

/**
 * A ControlEvent is used to control the simulation from the visualization GUI.
 *
 * @author Fred Sun
 */
public abstract class ControlEvent extends EventObject {

    /**
     * The generated serialVersionUID for the  serializable interface.
     */
    private static final long serialVersionUID = 4735230129230599313L;

    /**
     * Constructs a ControlEvent
     *
     * @param source The object on which the event initially occurred.
     */
    public ControlEvent(Object source) {
        super(source);
    }

}
