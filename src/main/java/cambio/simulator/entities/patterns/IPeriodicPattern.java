package cambio.simulator.entities.patterns;

import co.paralleluniverse.fibers.SuspendExecution;

/**
 * Marks a periodically appearing service owned pattern.
 *
 * @author Lion Wagner
 */
public interface IPeriodicPattern extends IPatternLifeCycleHooks {

    @Override
    void onInitializedCompleted();

    /**
     * Manually triggers this patterns' routine.
     */
    default void trigger() {
        onTriggered();
    }

    @Override
    default void shutdown() {
        PeriodicPatternScheduler scheduler = getScheduler();
        try {
            if (scheduler.isScheduled()) {
                scheduler.cancel();
            }
            scheduler.passivate();
        } catch (SuspendExecution suspendExecution) {
            suspendExecution.printStackTrace();
        }
    }

    void onTriggered();

    PeriodicPatternScheduler getScheduler();

}
