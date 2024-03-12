package cambio.simulator.entities.generator;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cambio.simulator.parsing.JsonTypeName;
import com.google.gson.annotations.SerializedName;
import desmoj.core.simulator.TimeInstant;

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
            throw new LoadGeneratorStopException();
        }
        if (Double.isNaN(load)) {
            this.load = 0;
        }

        return new IntervalArrivalRateModel();
    }

    public double getInterval() {
        return interval;
    }

    public void setInterval(double interval) {
        this.interval = interval;
    }

    private final class IntervalArrivalRateModel extends ArrivalRateModel {

        private long interArrivalTime;
        private int actualLoad;
        private int currentTimeInstantLoadCounter;

        private ScaleFactor scaleFactor = null;

        public IntervalArrivalRateModel() {
            if (loadDistribution.equals("even")) {
                this.interArrivalTime = (new TimeInstant(interval / load, TimeUnit.SECONDS)).getTimeInEpsilon();
                this.actualLoad = load == 0 ? 0 : 1;
            } else { //if (loadDistribution.equals("spike"))
                this.interArrivalTime = (new TimeInstant(interval, TimeUnit.SECONDS)).getTimeInEpsilon();
                this.actualLoad = (int) load;
            }
            this.currentTimeInstantLoadCounter = actualLoad;
        }

        private boolean requiresUpdate() {
            return scaleFactor != null;
        }

        // For even
        private void updateInterArrivalTime(final long currentTime) {
            this.interArrivalTime = (new TimeInstant(interval / (load * scaleFactor.getValue(currentTime)),
                TimeUnit.SECONDS)).getTimeInEpsilon();
        }

        // For spike
        private void updateLoad(final long currentTime) {
            this.actualLoad = (int) (load * scaleFactor.getValue(currentTime));
        }

        @Override
        protected long getDuration() {
            return Long.MAX_VALUE;
        }

        @Override
        protected void resetModelIteration() {
            currentTimeInstantLoadCounter = this.actualLoad;
        }

        @Override
        public void scaleLoad(ScaleFactor scaleFactor) {
            Objects.requireNonNull(scaleFactor);
            long currentTime = lastTimeInstant;
            currentTimeInstantLoadCounter = (int) (currentTimeInstantLoadCounter * scaleFactor.getValue(currentTime));
            this.scaleFactor = scaleFactor;
        }

        @Override
        public boolean hasNext() {
            return actualLoad > 0;
        }

        @Override
        public Long next() {
            if (!hasNext()) {
                return null;
            }
            if (finishedCurrentTimeInstant()) {
                handleUpdates();
                currentTimeInstantLoadCounter = actualLoad - 1;
                if (lastTimeInstant == null) {
                    return 0L;
                } else {
                    return lastTimeInstant + interArrivalTime;
                }
            } else {
                currentTimeInstantLoadCounter--;
                return lastTimeInstant;
            }
        }

        private void handleUpdates() {
            if (requiresUpdate()) {
                if (loadDistribution.equals("even")) {
                    updateInterArrivalTime(lastTimeInstant);
                } else { // spike
                    updateLoad(lastTimeInstant + interArrivalTime);
                }
            }
        }

        private boolean finishedCurrentTimeInstant() {
            return currentTimeInstantLoadCounter <= 0;
        }
    }
}
