package desmoj.extensions.visualEvents;

import java.util.EventListener;


/**
 * This is the basic listener interface for all the VisualEvents
 *
 * @author Fred Sun
 */
public interface VisualEventListener extends EventListener {

    /**
     * Invoked when a VisualEvent occurred.
     *
     * @param evt The occurred VisualEvent.
     */
    void visualEventReceived(VisualEvent evt);

}
