package cambio.simulator.misc;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * @author Lion Wagner
 */
public final class RNGMananger {

    private static final HashMap<String, Object> instances = new HashMap<>();

    public static <T> T get(String key, Supplier<T> supplier) {
        if (instances.containsKey(key)) {
            return (T) instances.get(key);
        } else {
            return (T) instances.computeIfAbsent(key, s -> supplier.get());
        }
    }

    public static void reset() {
        instances.clear();
    }
}