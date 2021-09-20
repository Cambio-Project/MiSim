package cambio.simulator.entities.generator;

import cambio.simulator.parsing.adapter.JsonTypeName;
import com.google.gson.annotations.SerializedName;

/**
 * @author Lion Wagner
 */
@JsonTypeName("interval")
public final class IntervalLoadGeneratorDescription extends LoadGeneratorDescription {

    @SerializedName(value = "interval", alternate = {"inter_arrival_time"})
    private double interval = 1;

    @SerializedName(value = "load", alternate = {"load_per_interval"})
    private double load = 1;

    @SerializedName(value = "distribution", alternate = {"dist_strat", "distribution_strategy", "dist_strategy"})
    private String loadDistribution = "even";


    @Override
    protected ArrivalRateModel createArrivalRateModel() {
        return new IntervalArrivalRateModel();
    }

    private final class IntervalArrivalRateModel extends ArrivalRateModel {

        private final transient double interArrivalTime;
        private final transient int actualLoad;
        private transient int currentLoadCounter;

        public IntervalArrivalRateModel() {
            if (loadDistribution.equals("even")) {
                this.interArrivalTime = interval / load;
                actualLoad = 1;
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
}
