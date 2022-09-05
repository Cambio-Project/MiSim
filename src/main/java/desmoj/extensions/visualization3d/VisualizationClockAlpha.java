package desmoj.extensions.visualization3d;

import org.scijava.java3d.Alpha;

/**
 * This Alpha uses the VisualizationClock instead of the system clock for speed control. Everything else is the same
 * like Alpha class.
 * </br></br>
 * For more information see <code>org.scijava.java3d.Alpha</code>
 *
 * @author Fred Sun
 */
public class VisualizationClockAlpha extends Alpha {

    //the clock for this alpha
    private final VisualizationClock _visClock;

    private final long _stopTime;

    /**
     * Constructs an VisualizationClockAlpha object with specified duration.
     *
     * @param duration The duration of this alpha.
     */
    public VisualizationClockAlpha(long duration) {
        super(1, duration);
        _visClock = VisualizationControl.getClock();

        long currentTime = _visClock.getTime();

        setStartTime(currentTime);
        _stopTime = currentTime + duration;
    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.Alpha#value()
     */
    @Override
    public float value() {
        return super.value(_visClock.getTime());
    }

    /* (non-Javadoc)
     * @see org.scijava.java3d.Alpha#finished()
     */
    public boolean finished() {
        long currentTime = _visClock.getTime();
        return currentTime > _stopTime;
    }


}
