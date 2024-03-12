package cambio.simulator.behavior;

import cambio.simulator.EventBus;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.events.HookEvent;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.models.MiSimModel;
import cambio.tltea.interpreter.nodes.cause.*;
import cambio.tltea.parser.core.temporal.TimeInstance;
import org.jetbrains.annotations.NotNull;

/**
 * Subscribes to specific events on the event bus.
 *
 * @author Lion Wagner
 */
public final class EventBusConnector {

    /**
     * Subscribe to events of interest.
     *
     * @param listener the listeners that should be provided updates to.
     * @param model the simulation model.
     */
    public static void createActivators(ValueListener<?> listener,
                                        @NotNull MiSimModel model) {
        if (listener instanceof EventActivationListener eventActivationListener) {
            String eventName = eventActivationListener.getValueOrEventName();
            if (eventName.contains(".fail")) {
                Microservice targetMS = NameResolver.resolveMicroserviceName(model,
                    eventActivationListener.getValueOrEventName().replace(".fail", ""));

                EventBus.subscribe(ChaosMonkeyEvent.class, (e) -> {
                    if (e.getTargetService().equals(targetMS)) {
                        eventActivationListener.activate(new TimeInstance(model.presentTime().getTimeAsDouble()));
                    }
                });
            } else if (eventName.startsWith("event.")) {
                String registeredName = eventName.replace("event.", "");
                EventBus.subscribe(HookEvent.class, (event) -> {
                    if (event.getData().getEventName().equals(registeredName)) {
                        eventActivationListener.updateValue(event.getValue(),
                            new TimeInstance(model.presentTime().getTimeAsDouble()));
                    }
                });
            }
        } else if (listener instanceof ConstantValueProvider<?>) { //no need to create activators for constants
            return;
        } else {
            System.out.println(listener);
        }
    }


}
