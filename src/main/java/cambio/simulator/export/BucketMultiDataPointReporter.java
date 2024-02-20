package cambio.simulator.export;

import java.util.function.UnaryOperator;

import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;

/**
 * A datapoint reporter that can map collected data points into time buckets based on a {@link #bucketingFunction}.
 * By default, no bucketing is applied.
 *
 * @author Sebastian Frank
 */
public abstract class BucketMultiDataPointReporter extends MultiDataPointReporter {
    protected final UnaryOperator<TimeInstant> bucketingFunction;
    public static UnaryOperator<TimeInstant> IDENTITY_FUNCTION = time -> time;
    /**
     * Maps the given time to the closest next bigger discrete time unit. Example: 1.3s is mapped to 2s.
     */
    public static UnaryOperator<TimeInstant> CEIL_FUNCTION = time -> new TimeInstant(Math.ceil(time.getTimeAsDouble()));

    /**
     * Constructs a reporter without bucketing.
     */
    public BucketMultiDataPointReporter(Model model) {
        this(model, IDENTITY_FUNCTION);
    }

    /**
     * Constructs a reporter without bucketing.
     *
     * @param datasetsPrefix {@link MultiDataPointReporter#MultiDataPointReporter(String, Model)}
     */
    public BucketMultiDataPointReporter(String datasetsPrefix, Model model) {
        this(datasetsPrefix, model, IDENTITY_FUNCTION);
    }

    /**
     * Constructs a reporter that applies bucketing of the added data.
     *
     * @param datasetsPrefix    {@link MultiDataPointReporter#MultiDataPointReporter(String, Model)}
     * @param bucketingFunction defines how the actual time is mapped to the bucket time, e.g., 1.3s to 1s for a
     *                          buckets of size 1s.
     */
    public BucketMultiDataPointReporter(String datasetsPrefix, Model model,
                                        UnaryOperator<TimeInstant> bucketingFunction) {
        super(datasetsPrefix, model);
        this.bucketingFunction = bucketingFunction;
    }


    /**
     * Constructs a reporter that applies bucketing of the added data.
     *
     * @param bucketingFunction defines how the actual time is mapped to the bucket time, e.g., 1.3s to 1s for a
     *                          buckets of size 1s.
     */
    public BucketMultiDataPointReporter(Model model, UnaryOperator<TimeInstant> bucketingFunction) {
        super(model);
        this.bucketingFunction = bucketingFunction;
    }

    @Override
    public <T> void addDatapoint(final String dataSetName, final TimeInstant when, final T... data) {
        super.addDatapoint(dataSetName, bucketingFunction.apply(when), data);
    }
}

