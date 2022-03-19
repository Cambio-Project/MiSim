package cambio.simulator.misc;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.jetbrains.annotations.NotNull;

/**
 * Class that holds static utility methods.
 *
 * <p>
 * Currently contains only utility methods to check values.
 *
 * @author Lion Wagner
 */
public final class Util {

    /**
     * Checks whether a number is greater than 0.
     *
     * @param number the value to check
     * @throws IllegalArgumentException if {@code number} is NOT &gt;0
     */
    public static void requireGreaterZero(Number number) {
        requireGreaterZero(number, "Argument has to be greater than 0.");
    }

    /**
     * Checks whether a number is greater than 0.
     *
     * @param number  the value to check
     * @param message The message that should be thrown if the number is not in the correct value range
     * @throws IllegalArgumentException if {@code number} is NOT &gt;0
     */
    public static void requireGreaterZero(Number number, String message) {
        if (!(number.doubleValue() > 0)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks whether a number is smaller than zero.
     *
     * @param number the value to check
     * @throws IllegalArgumentException if {@code number} is &lt;0
     */
    public static void requireNonNegative(Number number) {
        requireNonNegative(number, "Argument cannot be negative.");
    }


    /**
     * Checks whether a number is smaller than zero.
     *
     * @param number  the value to check
     * @param message The message that should be thrown if the number is not in the correct value range
     * @throws IllegalArgumentException if {@code number} is &lt;0
     */
    public static void requireNonNegative(Number number, String message) {
        if (number.doubleValue() < 0) {
            throw new IllegalArgumentException(message);
        }
    }


    /**
     * Checks whether a parameter is a percentage value.
     *
     * @param percentage the value to check
     * @throws IllegalArgumentException if {@code percentage} is not in [0,1]
     */
    public static void requirePercentage(double percentage) {
        requirePercentage(percentage, null);
    }

    /**
     * Checks whether a parameter is a percentage value.
     *
     * @param percentage the value to check
     * @param message    The message that should be thrown if the percentage is not in the correct value range
     * @throws IllegalArgumentException if {@code percentage} is not in [0,1]
     */
    public static void requirePercentage(double percentage, String message) {
        if (percentage < 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Formats a nanosecond time into a fixed time span format.
     *
     * @param nanosecs The amount of nanoseconds that should be parsed
     * @return A String that contains nanosecs value formated to days, hours, min, sec and ms
     */
    public static String timeFormat(long nanosecs) {
        long tempSec = nanosecs / (1000 * 1000 * 1000);
        long ms = (nanosecs / (1000 * 1000)) % 1000;
        long sec = tempSec % 60;
        long min = (tempSec / 60) % 60;
        long hour = (tempSec / (60 * 60)) % 24;
        long day = (tempSec / (24 * 60 * 60)) % 24;

        if (day > 0) {
            return String.format("%dd %dh %dm %ds %dms", day, hour, min, sec, ms);
        } else if (hour > 0) {
            return String.format("%dh %dm %ds %dms", hour, min, sec, ms);
        } else if (min > 0) {
            return String.format("%dm %ds %dms", min, sec, ms);
        } else if (sec > 0) {
            return String.format("%ds %dms", sec, ms);
        }
        return String.format("%dms", ms);
    }


    /**
     * Tries to inject a value into the field of an object via reflection. Checks declared fields of the objects class
     * and all its superclasses.
     *
     * @param fieldName name of the field
     * @param object    object that should be modified
     * @param newValue  value that should be injected
     */
    public static void injectField(String fieldName, @NotNull Object object, Object newValue) {
        try {
            Class<?> clazz = object.getClass();

            Field field = null;

            while (field == null && !(clazz == null)) {
                try {
                    field = clazz.getDeclaredField(fieldName);
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            if (field == null) {
                assert object.getClass() != null;
                throw new NoSuchFieldException(
                    String.format("Could not find find field %s on type %s or its super-classes.", fieldName,
                        object.getClass().getName()));
            }

            field.setAccessible(true);
            field.set(object, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the declared fields of this class and all of its Superclasses.
     *
     * @param clazz {@link Class} which fields should be
     * @return all {@link Field}s that are contained within the given {@link Class} and its superclasses.
     */
    public static Field[] getAllFields(Class<?> clazz) {
        ArrayList<Field> list = new ArrayList<>();
        Class<?> currentClass = clazz;

        while (currentClass != null) {
            list.addAll(Arrays.asList(currentClass.getDeclaredFields()));
            currentClass = currentClass.getSuperclass();
        }
        return list.toArray(new Field[0]);
    }
}
