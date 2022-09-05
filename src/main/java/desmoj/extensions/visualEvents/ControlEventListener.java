package desmoj.extensions.visualEvents;

import java.util.EventListener;


/**
 * This is the basic listener interface for all the ControlEvents
 *
 * @author Fred Sun
 */
public interface ControlEventListener extends EventListener {

    /**
     * Invoked when a ControlEvent occurred.
     *
     * @param evt The occurred ControlEvent.
     */
    void controlEventReceived(ControlEvent evt);
}
