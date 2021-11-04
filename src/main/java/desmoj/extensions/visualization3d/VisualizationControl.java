package desmoj.extensions.visualization3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import desmoj.extensions.visualEvents.ControlEvent;
import desmoj.extensions.visualEvents.ControlEventListener;
import desmoj.extensions.visualEvents.VisualEvent;
import desmoj.extensions.visualEvents.VisualEventListener;
import desmoj.extensions.visualEvents.VisualEventTransmitter;

/**
 * This class provides the central control unit for the spatial visualization. It's the connector between the
 * Visualevent-Producer and the VisualMudules. Each VisualModule is a different visualization implementation. The
 * VisualModule to be used should be registered in the VisualizationControler. The controller will forward the events to
 * each registered module.
 *
 * @author Fred Sun
 */
public class VisualizationControl implements VisualEventListener {

    //the clock of this VisualizationControl
    private static VisualizationClock _clock;
    //the container for all the VisualModules
    private final HashMap<String, VisualModule> _modules;
    //a list to contain the listeners
    private ArrayList<ControlEventListener> _listenerList = null;

    /**
     * Constructs a VisualControl.
     */
    public VisualizationControl() {
        _modules = new HashMap<String, VisualModule>();
        _clock = new VisualizationClock();
        //register this class to the VisualEventTransmitter
        VisualEventTransmitter.getVisualEventTransmitter().addVisualEventListener(this);
    }

    /**
     * Gets the clock instance which determines the time value for this VisualizationControl
     *
     * @return The VisualizationClock.
     */
    public static VisualizationClock getClock() {
        return _clock;
    }

    /**
     * Adds a ControlEventListener for receiving the ControlEvents from the VisualModules
     *
     * @param listener The listener to be added.
     */
    public void addControlEventListener(ControlEventListener listener) {
        if (_listenerList == null) {
            _listenerList = new ArrayList<ControlEventListener>();
        }
        _listenerList.add(listener);
    }

    /**
     * Registers a VisualModule to the VisualizationController.
     *
     * @param module The VisualModule to be registered.
     */
    public void addModule(VisualModule module) {
        String name = module.getModuleName();
        //check if a VisualModule with the same name already exist.
        if (_modules.containsKey(name)) {
            throw new IllegalArgumentException("A VisualModule with the same name already exist.");
        }
        module.setVisualizationControl(this);
        _modules.put(name, module);
    }

    /**
     * Fires a ControlEvent.
     *
     * @param evt The ControlEvent to be fired.
     */
    public void fireControlEvent(ControlEvent evt) {
        if (!_listenerList.isEmpty()) {
            Iterator<ControlEventListener> it = _listenerList.iterator();
            while (it.hasNext()) {
                it.next().controlEventReceived(evt);
            }
        }
    }

    /**
     * Gets a registered VisualModule with the given name. If no registered with the name can be found, null will be
     * returned.
     *
     * @param name The name of the registered VisualModule to be get.
     * @return The VisualModule with the name in this VisualizationControl. Null if there's none.
     */
    public VisualModule getModule(String name) {
        return _modules.get(name);
    }

    /**
     * Removes a registered ControlListener.
     *
     * @param listener The listener to be removed.
     */
    public void removeControlEventListener(ControlEventListener listener) {
        _listenerList.remove(listener);
    }

    /**
     * Removes the VisualModule with the name from the VisualizationControl if present.
     *
     * @param name The name of the VisualModule to be removed.
     */
    public void removeModule(String name) {
        if (_modules.containsKey(name)) {
            _modules.get(name).removeVisualizationControl();
            _modules.remove(name);
        }
    }

    /**
     * Sets the speed of the visualization clock to: real speed * rate.
     *
     * @param speed The execution speed rate. If the rate is <= 0, the clock will keep running with the old speed.
     */
    public void setExecutionSpeed(double rate) {
        if (rate > 0) {
            _clock.setRate(rate);
        }
    }

    /* (non-Javadoc)
     * @see desmoj.extensions.space3D.VisualEventListener#spatialEventReceived(desmoj.extensions.space3D.VisualEvent)
     */
    public void visualEventReceived(VisualEvent evt) {
        Iterator<VisualModule> modules = _modules.values().iterator();
        while (modules.hasNext()) {
            modules.next().handleEvent(evt);
        }
    }
}
