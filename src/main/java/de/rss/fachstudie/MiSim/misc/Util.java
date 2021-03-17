package de.rss.fachstudie.MiSim.misc;

/**
 * Class that holds static utility methods.
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

}
