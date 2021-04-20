package de.rss.fachstudie.MiSim.misc;

/**
 * Class that holds static utility methods.
 * <p>
 * Currently contains only utility methods to check values.
 *
 * @author Lion Wagner
 */
public class Util {

    public static void requireGreaterZero(Number number) {
        requireGreaterZero(number, "Argument has to be greater than 0.");
    }


    public static void requireGreaterZero(Number number, String message) {
        if (number.doubleValue() < 1) {
            throw new IllegalArgumentException("Argument has to be greater than 0.");
        }
    }


    public static void requireNonNegative(Number number) {
        requireNonNegative(number, "Argument cannot be negative.");
    }

    public static void requireNonNegative(Number number, String message) {
        if (number.doubleValue() < 0) {
            throw new IllegalArgumentException("Argument cannot be negative.");
        }
    }

    public static void requirePercentage(double probability) {
        requirePercentage(probability, null);
    }

    public static void requirePercentage(double probability, String message) {
        if (probability < 0.0 || probability > 1.0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static String timeFormat(long nanosecs) {
        long tempSec = nanosecs / (1000 * 1000 * 1000);
        long ms = (nanosecs / (1000 * 1000)) % 1000;
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;

        if (day > 0)
            return String.format("%dd %dh %dm %ds %dms", day, hour, min, sec, ms);
        else if (hour > 0)
            return String.format("%dh %dm %ds %dms", hour, min, sec, ms);
        else if (min > 0)
            return String.format("%dm %ds %dms", min, sec, ms);
        else if (sec > 0)
            return String.format("%ds %dms", sec, ms);
        return String.format("%dms", ms);
    }

}
