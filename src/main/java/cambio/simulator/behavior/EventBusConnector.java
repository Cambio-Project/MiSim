package cambio.simulator.behavior;

import cambio.simulator.EventBus;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.models.MiSimModel;
import cambio.tltea.interpreter.nodes.cause.*;
import cambio.tltea.parser.core.temporal.TimeInstance;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lion Wagner
 */
public final class EventBusConnector {
    public static void createActivators(ValueListener<?> listener,
                                        @NotNull MiSimModel model) {
        if (listener instanceof EventActivationListener eventActivationListener) {
            if (eventActivationListener.getValueOrEventName().contains(".fail")) {
                Microservice targetMS = NameResolver.resolveMicroserviceName(model,
                    eventActivationListener.getValueOrEventName().replace(".fail", ""));

                EventBus.subscribe(ChaosMonkeyEvent.class, (e) -> {
                    if (e.getTargetService().equals(targetMS)) {
                        eventActivationListener.activate(new TimeInstance(model.presentTime().getTimeAsDouble()));
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
