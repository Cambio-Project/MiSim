package cambio.simulator.test;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.math3.stat.descriptive.rank.Percentile;

/**
 * @author Lion Wagner
 */
public class StatisticsUtils {


    public static double median(final Collection<? extends Number> number_collection) {
        return percentile(.5, number_collection);
    }

    public static double mean(final Collection<? extends Number> number_collection) {
        return number_collection.stream().mapToDouble(Number::doubleValue).average().orElse(-1);
    }

    public static double median(Number... number_collection) {
        return percentile(.5, Arrays.asList(number_collection));
    }

    public static double mean(Number... number_collection) {
        return Arrays.stream(number_collection).mapToDouble(Number::doubleValue).average().orElse(-1);
    }

    /**
     * Calculates a specific percentile of a collection of numbers.
     *
     * @param percentile        target percentile in [0:100)
     * @param number_collection collection containing the analysed dataset
     * @return the asked percentile
     * @throws org.apache.commons.math3.exception.MathIllegalArgumentException if percentile not in [0:100)
     * @see Percentile
     */
    public static double percentile(final double percentile, final Collection<? extends Number> number_collection) {
        double[] sortedData = number_collection.stream().mapToDouble(Number::doubleValue).sorted().toArray();
        return new Percentile(percentile).evaluate(sortedData);
    }
}
