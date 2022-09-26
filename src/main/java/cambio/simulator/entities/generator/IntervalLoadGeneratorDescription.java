package cambio.simulator.entities.generator;

import cambio.simulator.parsing.JsonTypeName;
import com.google.gson.annotations.SerializedName;

/**
 * Adds properties to a {@link LoadGeneratorDescription} for describing an interval-based load generator.
 *
 * <p>
 * A generator of this type sends requests in intervals, either evenly distributed or in reoccurring spikes.
 *
 * @author Lion Wagner
 */
@JsonTypeName(value = "interval", alternativeNames = {"constant", "fixed"})
public final class IntervalLoadGeneratorDescription extends LoadGeneratorDescription {

    @SerializedName(value = "interval", alternate = {"inter_arrival_time"})
    private double interval = 1;

    @SerializedName(value = "load", alternate = {"load_per_interval"})
    private double load = 1;

    @SerializedName(value = "distribution", alternate = {"dist_strat", "distribution_strategy", "dist_strategy"})
    private String loadDistribution = "even";


    @Override
    protected ArrivalRateModel createArrivalRateModel() {
        if (interval <= 0 || interval == Double.POSITIVE_INFINITY) {
            throw new IllegalArgumentException("Interval has to be greater than 0 and not infinite.");
        }
        if (Double.isNaN(interval)) {
            System.out.printf("[Warning] Interval '%s' is not a valid value (in (0 - %s]). "
                + "An interval generator will not be started.%n", interval, Double.MAX_VALUE);
            this.interval = 1;
            this.load = 0;
        }
        if (Double.isNaN(load)) {
            this.load = 0;
        }

        return new IntervalArrivalRateModel();
    }

    private final class IntervalArrivalRateModel extends ArrivalRateModel {

        private double interArrivalTime;
        private int actualLoad;
        private int currentLoadCounter;

        public IntervalArrivalRateModel() {
            if (loadDistribution.equals("even")) {
                this.interArrivalTime = interval / load;
                actualLoad = load == 0 ? 0 : 1;
            } else { //if (loadDistribution.equals("spike"))
                this.interArrivalTime = interval;
                this.actualLoad = (int) load;
            }
        }

        @Override
        protected double getDuration() {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        protected void resetModelIteration() {
            currentLoadCounter = 0;
        }

        @Override
        public void scaleLoad(double scaleFactor) {
            currentLoadCounter = (int) (currentLoadCounter * scaleFactor);
            actualLoad = (int) (actualLoad * scaleFactor);

            if (loadDistribution.equals("even")) {
                interArrivalTime = 1.0 / actualLoad;
            }
        }

        @Override
        public boolean hasNext() {
            return actualLoad > 0;
        }

        @Override
        public Double next() {
            if (!hasNext()) {
                return null;
            }
            if (currentLoadCounter <= 0) {
                currentLoadCounter = actualLoad - 1;
                if (lastTimeInstant == null) {
                    return 0.0;
                } else {
                    return lastTimeInstant + interArrivalTime;
                }
            } else {
                currentLoadCounter--;
                return lastTimeInstant;
            }
        }
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }
}
