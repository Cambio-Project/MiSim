package cambio.simulator.behavior;

import java.util.*;

import cambio.simulator.entities.generator.*;
import cambio.simulator.entities.microservice.MicroserviceScaleEvent;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.misc.NameResolver;
import cambio.simulator.misc.Util;
import cambio.simulator.models.MiSimModel;
import cambio.tltea.interpreter.nodes.ISubscribableTriggerNotifier;
import cambio.tltea.interpreter.nodes.consequence.activation.*;
import cambio.tltea.parser.core.OperatorToken;
import cambio.tltea.parser.core.temporal.*;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;

/**
 * Listener that takes care of handling the activation of MTL events.
 *
 * @author Lion Wagner
 */
public class MTLActivationListener {

    private final MiSimModel model;
    private final ISubscribableTriggerNotifier triggerNotifier;

    /**
     * Creates a new instance of an MTL listener.
     *
     * @param triggerNotifier Notifier that will be listend to.
     * @param model           Model that will be used to resolve the names.
     */
    public MTLActivationListener(ISubscribableTriggerNotifier triggerNotifier, MiSimModel model) {
        this.model = model;
        this.triggerNotifier = triggerNotifier;
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onEventActivation, EventActivationData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onEventPrevention, EventPreventionData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onValueEvent, ValueEventActivationData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onServiceStartEvent, ServiceStartEventData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onServiceStopEvent, ServiceStopEventData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onServiceKillEvent, ServiceFailureEventData.class);
        this.triggerNotifier.subscribeEventListenerWithFilter(this::onLoadEvent, LoadModificationEventData.class);
        this.triggerNotifier.subscribeEventListener(
            activationData -> System.out.println("Event activated: " + activationData));
    }

    private Optional<TimeInstant> tryFindStartTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return Optional.of(new TimeInstant(moment.getTime()));
        }

        if (token == OperatorToken.FINALLY) {
            if (time instanceof TemporalInterval interval) {
                var random = new Random();
                var delay = Math.min(
                    random.nextDouble() * interval.getDuration(),
                    model.getExperimentMetaData().getDuration());
                return Optional.of(new TimeInstant(model.presentTime().getTimeAsDouble() + delay));
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else if (token == OperatorToken.GLOBALLY) {
            if (time instanceof TemporalInterval interval) {
                return Optional.of(new TimeInstant(interval.getStart()));
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else {
            System.out.println("Unsupported temporal operator: " + token);
        }

        return Optional.empty();
    }


    private Optional<TimeInstant> tryFindStopTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return Optional.of(new TimeInstant(moment.getTime()));
        } else if (time instanceof TemporalInterval interval) {
            return Optional.of(new TimeInstant(interval.getEnd()));
        } else {
            System.out.println("Unsupported temporal expression: " + time);
        }

        return Optional.of(new TimeInstant(Double.POSITIVE_INFINITY));
    }

    private void onEventActivation(EventActivationData data) {
        System.out.println("Event activated: " + data);

        if (data.getData().contains("start")) {
            var split = data.getData().split("\\.");
            NameResolver.resolveMicroserviceName(model, split[0]).setInstancesCount(1);
        }
    }

    private void onEventPrevention(EventPreventionData data) {
        System.out.println("Event activated: " + data);
    }

    private void onValueEvent(ValueEventActivationData<?> data) {
        System.out.println("Event activated: " + data);
    }

    private void onServiceStartEvent(ServiceStartEventData data) {
        System.out.println("Event activated: " + data);
    }

    private void onServiceStopEvent(ServiceStopEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());

        if (targetTime.isEmpty()) {
            System.out.println("Could not find target time for service failure event. " + data.getTemporalContext());
            return;
        }

        var service = NameResolver.resolveMicroserviceName(model, data.getServiceName());
        MicroserviceScaleEvent event = new MicroserviceScaleEvent(model, "Forced Shutdown", true, service, 0);
        event.schedule(targetTime.get());
    }

    private void onServiceKillEvent(ServiceFailureEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());

        if (targetTime.isEmpty()) {
            System.out.println("Could not find target time for service failure event. " + data.getTemporalContext());
            return;
        }

        var target = NameResolver.resolveMicroserviceName(model, data.getServiceName());
        var killer = new ChaosMonkeyEvent(model, "MTLChaosmonkey", true);
        Util.injectField("microservice", killer, target);
        killer.schedule(targetTime.get());
    }


    private void onLoadEvent(LoadModificationEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());
        var stopTime = tryFindStopTime(data.getTemporalContext());
        var targetName = data.getLoad_str();
        var targetOperations = new ArrayList<Operation>();


        if (targetTime.isEmpty()) {
            System.out.println("Could not find target time for service failure event. " + data.getTemporalContext());
            return;
        }

        var targetDouble = targetTime.get().getTimeAsDouble();
        var duration = new TimeSpan(stopTime.get().getTimeAsDouble() - targetDouble);

        if (targetName.contains(".")) {
            var targetOperation = NameResolver.resolveOperationName(model, targetName);
            targetOperations.add(targetOperation);
        } else {
            var tmp = NameResolver.resolveMicroserviceName(model, targetName).getOperations();
            targetOperations.addAll(List.of(tmp));
        }

        if (data.isFactor()) {
            model.getExperimentModel()
                .getAllSelfSchedulesEntities()
                .stream()
                .filter(e -> e instanceof LoadGeneratorDescriptionExecutor)
                .map(e -> (LoadGeneratorDescriptionExecutor) e)
                .filter(exec -> targetOperations.contains(exec.getLoadGeneratorDescription().getTargetOperation()))
                .map(exec ->
                    new ScaleLoadEvent(model, "Load Scaling", true, exec, data.getModificationValue(), duration))
                .forEach(event -> event.schedule(targetTime.get()));
        } else {
            var targetLoad = data.getModificationValue() / targetOperations.size();

            for (var operation : targetOperations) {
                var generatorDescription = new IntervalLoadGeneratorDescription();
                Util.injectField("name", generatorDescription, "TemporaryGenerator");
                Util.injectField("targetOperation", generatorDescription, operation);
                Util.injectField("initialArrivalTime", generatorDescription, targetTime.get().getTimeAsDouble());
                Util.injectField("stopTime", generatorDescription, stopTime.get().getTimeAsDouble());
                Util.injectField("load", generatorDescription, targetLoad);
                generatorDescription.initializeArrivalRateModel();

                var executor = new LoadGeneratorDescriptionExecutor(model, generatorDescription);
                executor.doInitialSelfSchedule();
                model.getExperimentModel().getAllSelfSchedulesEntities().add(executor);
            }
        }
    }
}
