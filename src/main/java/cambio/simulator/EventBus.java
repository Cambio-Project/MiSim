package cambio.simulator;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Consumer;

import desmoj.core.simulator.Schedulable;
import org.jetbrains.annotations.NotNull;

/**
 * Static class that takes care of enabling subscribing and publishing events.
 *
 * @author Lion Wagner
 */
public class EventBus {

    private static final Map<Class<? extends Schedulable>, LinkedList<Consumer<Schedulable>>> listeners =
        new HashMap<>();

    public static void post(@NotNull Schedulable event) {
        listeners.getOrDefault(event.getClass(), new LinkedList<>()).forEach(listener -> listener.accept(event));
    }

    public static <T extends Schedulable> void subscribe(Class<T> targetClass, Consumer<T> consumer) {
        //noinspection unchecked
        listeners.computeIfAbsent(targetClass, k -> new LinkedList<>()).add((Consumer<Schedulable>) consumer);
    }

    // TODO: This is a hotfix to avoid having a state from one simulation carry over to the next one. Actually,
    //  static classes are an anti-pattern that should not be used exactly for this reason!
    public static void clear() {
        listeners.clear();
    }

}
