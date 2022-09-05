package desmoj.extensions.visualEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is responsible for firing the SpatialEvents. It is a singleton, so every listener only have to be assigned
 * once.
 *
 * @author Fred Sun
 */
public class VisualEventTransmitter {

    //the static singleton VisualEventTransmitter
    private static VisualEventTransmitter _transmitter = null;
    //A ArrayList which holds the registered Listeners.
    private final List<VisualEventListener> _listeners = new ArrayList<VisualEventListener>();

    /**
     * The static get method for the VisualEventTransmitter
     *
     * @return The singleton VisualEventTransmitter
     */
    public static VisualEventTransmitter getVisualEventTransmitter() {
        if (_transmitter == null) {
            _transmitter = new VisualEventTransmitter();
        }
        return _transmitter;
    }

    /**
     * Registers a new VisualEventListener to the SpatialMovementManager.
     *
     * @param listener The listener to be registered.
     */
    public void addVisualEventListener(VisualEventListener listener) {
        _listeners.add(listener);
    }

    /**
     * Removes a registered VisualEventListener.
     *
     * @param listener The listener to be removed.
     */
    public void removeVisualEventListener(VisualEventListener listener) {
        _listeners.remove(listener);
    }

    /**
     * Fires a VisualEvent.
     *
     * @param evt The VisualEvent to be fired.
     */
    public void fireVisualEvent(VisualEvent evt) {
        if (!_listeners.isEmpty()) {
            Iterator<VisualEventListener> it = _listeners.iterator();
            while (it.hasNext()) {
                it.next().visualEventReceived(evt);
            }
        }
    }
}
