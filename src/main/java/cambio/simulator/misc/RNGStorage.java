package cambio.simulator.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.*;

/**
 * This class should be used to keep track of all static Random Number Generators (RNG). However, it will accept all
 * types and tries to guarantee type correctness based on the given {@link Supplier}.
 *
 * <p>
 * Technically it is a wrapper for a HashMap that only allows the {@link Map#computeIfAbsent(Object, Function)} ({@link
 * #get(Object, Supplier)}) and {@link Map#clear()} ({@link #reset()}) methods.
 *
 * @author Lion Wagner
 */
public final class RNGStorage {

    private static final Map<Object, Object> instances = new HashMap<>();

    /**
     * Returns the value associated with the given key. If the key is not present, the supplier will be used to create a
     * new value.
     *
     * @param key      the key to look up
     * @param supplier a supplier that will be used to create a new value if the key is not present, ignored otherwise.
     * @param <T>      the type of the value that is created by the supplier.
     * @return the value associated with the given key.
     * @throws ClassCastException if the given key is associated with a value of a different type than the supplier
     *                            returns.
     */
    @Contract("_, !null -> !null")
    public static synchronized <T> T get(@NotNull final Object key, @NotNull final Supplier<T> supplier) {
        return (T) instances.computeIfAbsent(key, s -> supplier.get());
    }

    /**
     * Grants the information whether the key is present in storage and the type of the value associated with the given
     * key.
     *
     * @return the type associated with the given key or {@code null} otherwise.
     */

    @Contract(pure = true)
    public static @Nullable Class<?> containsKey(@NotNull final Object key) {
        return instances.containsKey(key) ? instances.get(key).getClass() : null;
    }

    /**
     * Resets/Removes all key-value pairs.
     */
    public static void reset() {
        instances.clear();
        System.out.println("[DEBUG] RNGStorage cleared.");
    }
}