package cambio.simulator.behavior;

import java.util.*;

import cambio.simulator.entities.generator.*;
import cambio.simulator.entities.microservice.MicroserviceScaleEvent;
import cambio.simulator.entities.microservice.Operation;
import cambio.simulator.events.ChaosMonkeyEvent;
import cambio.simulator.events.HookEvent;
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

    /**
     * Creates a new instance of an MTL listener.
     *
     * @param triggerNotifier Notifier that will be listend to.
     * @param model           Model that will be used to resolve the names.
     */
    public MTLActivationListener(ISubscribableTriggerNotifier triggerNotifier, MiSimModel model) {
        this.model = model;
        triggerNotifier.subscribeEventListenerWithFilter(this::onEventActivation, EventActivationData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onEventPrevention, EventPreventionData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onValueEvent, ValueEventActivationData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onServiceStartEvent, ServiceStartEventData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onServiceStopEvent, ServiceStopEventData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onServiceKillEvent, ServiceFailureEventData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onLoadEvent, LoadModificationEventData.class);
        triggerNotifier.subscribeEventListenerWithFilter(this::onHookEvent, HookEventData.class);
        triggerNotifier.subscribeEventListener(
            activationData -> System.out.println("Event activated: " + activationData));
    }

    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private Optional<TimeInstant> tryFindStartTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return Optional.of(new TimeInstant(moment.getTime()));
        }

        if (token == OperatorToken.FINALLY) {
            if (time instanceof TemporalInterval interval) {
                var random = new Random();
                var delay = random.nextDouble() * interval.getDuration();
                var startTime = Math.min((model.presentTime().getTimeAsDouble() + interval.getStart() + delay),
                    model.getExperimentMetaData().getDuration());
                return Optional.of(new TimeInstant(startTime));
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else if (token == OperatorToken.GLOBALLY) {
            if (time instanceof TemporalInterval interval) {
                return Optional.of(new TimeInstant(model.presentTime().getTimeAsDouble() + interval.getStart()));
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else {
            System.out.println("Unsupported temporal operator: " + token);
        }

        return Optional.empty();
    }

    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private Optional<TimeInstant> tryFindStopTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return Optional.of(new TimeInstant(moment.getTime() + model.presentTime().getTimeAsDouble()));
        } else if (time instanceof TemporalInterval interval) {
            return Optional.of(new TimeInstant(interval.getEnd() + model.presentTime().getTimeAsDouble()));
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

    private ArrayList<Operation> findOperations(String targetName) {
        var targetOperations = new ArrayList<Operation>();
        if (targetName.contains(".")) {
            var targetOperation = NameResolver.resolveOperationName(model, targetName);
            targetOperations.add(targetOperation);
        } else {
            var tmp = NameResolver.resolveMicroserviceName(model, targetName).getOperations();
            targetOperations.addAll(List.of(tmp));
        }
        return targetOperations;
    }

    private void onLoadEvent(LoadModificationEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());
        var stopTime = tryFindStopTime(data.getTemporalContext());
        var targetName = data.getLoad_str();
        var targetOperations = findOperations(targetName);

        if (targetTime.isEmpty() || stopTime.isEmpty()) {
            System.out.println("Could not find target time for service failure event. " + data.getTemporalContext());
            return;
        }

        var targetDouble = targetTime.get().getTimeAsDouble();
        var stopDouble = stopTime.get().getTimeAsDouble();
        var durationDouble = stopDouble - targetDouble;
        var duration = new TimeSpan(durationDouble);

        if (data.isFactor()) {
            ScaleFactor scaleFactor = new ScaleFactor(data.getModificationValue(), targetDouble, durationDouble,
                ScaleFunction.detect(data.getFunctionType()));

            model.getExperimentModel()
                .getAllSelfSchedulesEntities()
                .stream()
                .filter(e -> e instanceof LoadGeneratorDescriptionExecutor)
                .map(e -> (LoadGeneratorDescriptionExecutor) e)
                .filter(exec -> targetOperations.contains(exec.getLoadGeneratorDescription().getTargetOperation()))
                .map(exec ->
                    new ScaleLoadEvent(model, "Load Scaling", true, exec, scaleFactor, duration))
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

    private void onHookEvent(HookEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());
        var stopTime = tryFindStopTime(data.getTemporalContext());

        if (targetTime.isEmpty() || stopTime.isEmpty()) {
            System.out.println("Could not find target time for named event. " + data.getTemporalContext());
            return;
        }

        var targetDouble = targetTime.get().getTimeAsDouble();
        var stopDouble = stopTime.get().getTimeAsDouble();
        var durationDouble = stopDouble - targetDouble;
        var duration = new TimeSpan(durationDouble);

        HookEvent startEvent = new HookEvent(model, data, true, false);
        startEvent.schedule(targetTime.get());
        HookEvent stopEvent = new HookEvent(model, data, false, false);
        stopEvent.schedule(stopTime.get());
    }
}