package desmoj.extensions.visualization3d;

/**
 * A clock who provides the modified time value for the VisualModules controlled by the VisualizationControl.
 * </br>
 * The speed of the clock can be changed through a speed rate
 * </br>
 * The influence of the rate to the clock speed is:</br> clock speed = system clock speed * speed rate
 *
 * @author Fred Sun
 */
public class VisualizationClock {

    //the time of the last update in modified time
    private long _lastUpdatePointNewTime;
    //the time of the last update in system time
    private long _lastUpdatePointSystemTime;
    //the speed rate of the time
    private double _rate;

    /**
     * Constructs a new ViaualizationClock with speed rate of 1.
     */
    public VisualizationClock() {
        _lastUpdatePointNewTime = 0l;
        _rate = 1.0;
        _lastUpdatePointSystemTime = System.currentTimeMillis();
    }

    /**
     * Returns the time value.
     *
     * @return The time value.
     */
    public long getTime() {
        long delta = System.currentTimeMillis() - _lastUpdatePointSystemTime;
        return _lastUpdatePointNewTime + ((long) (delta * _rate));
    }

    /**
     * Gets the current speed rate.
     *
     * @return The current speed rate
     */
    public double getRate() {
        return _rate;
    }

    /**
     * Sets the speed rate, which influence the clock speed with:</br> clock speed = system clock speed * speed rate
     *
     * @param rate The speed rate.
     */
    public void setRate(double rate) {
        _lastUpdatePointNewTime = this.getTime();
        _lastUpdatePointSystemTime = System.currentTimeMillis();
        _rate = rate;
    }
}
