package cambio.simulator.entities.generator;

import java.util.Objects;

import cambio.simulator.misc.Util;

/**
 * Describes time-dependent scaling of load.
 */
public class ScaleFactor {
    private final double startTime;
    private final double endTime;
    private final double duration;
    private double targetScaleFactor;
    private double startScaleFactor = 1.0;

    private ScaleFunction scaleFunction = s -> targetScaleFactor; // constant

    /**
     * Creates a scaling factor based on the specification of a duration and the default scaling function (constant).
     *
     * @param scaleFactor A value > 0 that describes how much the load should be scaled at the peak of the scaling
     *                    function.
     * @param startTime   The time in the simulation at which the scaling should start.
     * @param duration    How long scaling should be applied. Must be >0.
     */
    public ScaleFactor(final double scaleFactor, final double startTime, final double duration) {
        Util.requireGreaterZero(scaleFactor);
        Util.requireGreaterZero(duration);
        Util.requireNonNegative(startTime);
        this.targetScaleFactor = scaleFactor;
        this.startTime = startTime;
        this.duration = duration;
        this.endTime = startTime + duration;
    }

    /**
     * Creates a scaling factor beginning with a specified value based on the specification of a duration and the
     * default scaling function (constant).
     *
     * @param startScaleFactor The scaling factor a which the scaling should start. By default, it is 1.
     * @param scaleFactor      A value > 0 that describes how much the load should be scaled at the peak of the
     *                         scaling function.
     * @param startTime        The time in the simulation at which the scaling should start.
     * @param duration         How long scaling should be applied. Must be >0.
     */
    public ScaleFactor(final double startScaleFactor, final double scaleFactor, final double startTime,
                       final double duration) {
        this(scaleFactor, startTime, duration);
        Util.requireGreaterZero(startScaleFactor);
        this.startScaleFactor = startScaleFactor;
    }

    /**
     * Creates a scaling factor based on the specification of a duration and a custom scaling function.
     *
     * @param scaleFactor   A value > 0 that describes how much the load should be scaled at the peak of the scaling
     *                      function.
     * @param startTime     The time in the simulation at which the scaling should start.
     * @param duration      How long scaling should be applied. Must be >0.
     * @param scaleFunction describes the shape of the scaling behavior.
     */
    public ScaleFactor(final double scaleFactor, final double startTime, final double duration,
                       final ScaleFunction scaleFunction) {
        this(scaleFactor, startTime, duration);
        Objects.requireNonNull(scaleFunction);
        this.scaleFunction = scaleFunction;
    }

    /**
     * Creates a scaling factor beginning with a specified value based on the specification of a duration and a
     * custom scaling function.
     *
     * @param startScaleFactor The scaling factor a which the scaling should start. By default, it is 1.
     * @param scaleFactor      A value > 0 that describes how much the load should be scaled at the peak of the
     *                         scaling function.
     * @param startTime        The time in the simulation at which the scaling should start.
     * @param duration         How long scaling should be applied. Must be >0.
     * @param scaleFunction    describes the shape of the scaling behavior.
     */
    public ScaleFactor(final double startScaleFactor, final double scaleFactor, final double startTime,
                       final double duration, final ScaleFunction scaleFunction) {
        this(startScaleFactor, scaleFactor, startTime, duration);
        Objects.requireNonNull(scaleFunction);
        this.scaleFunction = scaleFunction;
    }

    public ScaleFunction getScaleFunction() {
        return scaleFunction;
    }

    public void setScaleFunction(ScaleFunction scaleFunction) {
        this.scaleFunction = scaleFunction;
    }

    /**
     * Returns the scaling factor for a given point in time.
     *
     * @param currentTime the point in time at which the factor is required.
     * @return >= 0
     */
    public double getValue(final double currentTime) {
        if (currentTime > endTime || currentTime < startTime) {
            return 1;
        } else {
            double progressAbsolute = currentTime - startTime;
            double progressRelative = progressAbsolute / duration;
            return startScaleFactor + scaleFunction.map(progressRelative) * (targetScaleFactor - startScaleFactor);
        }
    }

}
