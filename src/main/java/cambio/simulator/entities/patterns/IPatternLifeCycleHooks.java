package cambio.simulator.entities.patterns;

import desmoj.core.simulator.Model;

/**
 * Represents all pattern lifecycle hooks.
 *
 * @author Lion Wagner
 */
public interface IPatternLifeCycleHooks {

    /**
     * Will be called after completion of the initialization right after the config was injected.
     */
    default void onInitializedCompleted(Model model) {
    }

    /**
     * Will be called once the owning entity is ready to communicate.
     */
    default void start() {
    }

    /**
     * Will be called by the owning instance upon an unexpected shutdown (kill).
     * TODO: distinguish killed and shutdown better, this method will only represent a soft shutdown later
     */
    default void shutdown() {
    }
}
