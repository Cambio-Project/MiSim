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

    private TimeInstant sum(TimeInstant time1, TimeInstant time2) {
        return new TimeInstant(
            Math.min((time1.getTimeAsDouble() + time2.getTimeAsDouble() - model.presentTime().getTimeAsDouble()),
                model.getExperimentMetaData().getDuration()));
    }

    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private TimeInstant tryFindStartTime(ITemporalValue time) {
        if (time instanceof TimeInstance moment) {
            return new TimeInstant(model.presentTime().getTimeAsDouble() + moment.getTime());
        } else if (time instanceof TemporalInterval interval) {
            return new TimeInstant(model.presentTime().getTimeAsDouble() + interval.getStart());
        } else {
            System.out.println("Unsupported temporal expression: " + time);
        }
        return new TimeInstant(model.presentTime().getTimeAsDouble());
    }

    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private TimeInstant tryFindStopTime(ITemporalValue time) {
        if (time instanceof TimeInstance moment) {
            return new TimeInstant(moment.getTime() + model.presentTime().getTimeAsDouble());
        } else if (time instanceof TemporalInterval interval) {
            return new TimeInstant(interval.getEnd() + model.presentTime().getTimeAsDouble());
        } else {
            System.out.println("Unsupported temporal expression: " + time);
        }
        return new TimeInstant(Double.POSITIVE_INFINITY);
    }


    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private TimeInstant tryFindStartTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return new TimeInstant(model.presentTime().getTimeAsDouble() + moment.getTime());
        }

        if (token == OperatorToken.FINALLY) {
            if (time instanceof TemporalInterval interval) {
                var random = new Random();
                var delay = random.nextDouble() * interval.getDuration();
                var startTime = Math.min((model.presentTime().getTimeAsDouble() + interval.getStart() + delay),
                    model.getExperimentMetaData().getDuration());
                return new TimeInstant(startTime);
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else if (token == OperatorToken.GLOBALLY) {
            if (time instanceof TemporalInterval interval) {
                return new TimeInstant(model.presentTime().getTimeAsDouble() + interval.getStart());
            } else {
                System.out.println("Unsupported temporal expression: " + time);
            }
        } else {
            System.out.println("Unsupported temporal operator: " + token);
        }
        // return current time
        return new TimeInstant(model.presentTime().getTimeAsDouble());
    }

    // TODO: Needs adjustments! Fix relative time, delay in F_end, simulation time limit, ...
    private TimeInstant tryFindStopTime(TemporalOperatorInfo info) {
        var token = info.operator();
        var time = info.temporalValueExpression();


        if (time instanceof TimeInstance moment) {
            return new TimeInstant(moment.getTime() + model.presentTime().getTimeAsDouble());
        } else if (time instanceof TemporalInterval interval) {
            return new TimeInstant(interval.getEnd() + model.presentTime().getTimeAsDouble());
        } else {
            System.out.println("Unsupported temporal expression: " + time);
        }

        return new TimeInstant(Double.POSITIVE_INFINITY);
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

        var service = NameResolver.resolveMicroserviceName(model, data.getServiceName());
        int instanceCount = data.getCount();
        MicroserviceScaleEvent event =
            new MicroserviceScaleEvent(model, "Forced Shutdown", true, service, instanceCount);
        event.schedule(targetTime);
    }

    private void onServiceKillEvent(ServiceFailureEventData data) {
        var targetTime = tryFindStartTime(data.getTemporalContext());

        var target = NameResolver.resolveMicroserviceName(model, data.getServiceName());
        int instanceCount = data.getCount();
        var killer = new ChaosMonkeyEvent(model, "MTLChaosmonkey", true, target, instanceCount);
        killer.schedule(targetTime);
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
        var targetTime = sum(tryFindStartTime(data.getTemporalContext()), tryFindStartTime(data.getDuration()));
        var stopTime = sum(tryFindStopTime(data.getTemporalContext()), tryFindStopTime(data.getDuration()));
        var targetName = data.getLoad_str();
        var targetOperations = findOperations(targetName);

        var targetDouble = targetTime.getTimeAsDouble();
        var stopDouble = stopTime.getTimeAsDouble();
        var durationDouble = stopDouble - targetDouble;
        var duration = new TimeSpan(durationDouble);

        if (data.isFactor()) {
            String functionType = data.getFunctionType();
            ScaleFunction scaleFunction;
            if (functionType.endsWith("-inverse")) {
                scaleFunction = ScaleFunction.revert(ScaleFunction.detect(functionType.replace("-inverse", "")));
            } else {
                scaleFunction = ScaleFunction.detect(functionType);
            }

            ScaleFactor scaleFactor = new ScaleFactor(data.getModificationValue(), targetDouble, durationDouble,
                scaleFunction);

            model.getExperimentModel()
                .getAllSelfSchedulesEntities()
                .stream()
                .filter(e -> e instanceof LoadGeneratorDescriptionExecutor)
                .map(e -> (LoadGeneratorDescriptionExecutor) e)
                .filter(exec -> targetOperations.contains(exec.getLoadGeneratorDescription().getTargetOperation()))
                .map(exec ->
                    new ScaleLoadEvent(model, "Load Scaling", true, exec, scaleFactor, duration))
                .forEach(event -> event.schedule(targetTime));
        } else {
            var targetLoad = data.getModificationValue() / targetOperations.size();

            for (var operation : targetOperations) {
                var generatorDescription = new IntervalLoadGeneratorDescription();
                Util.injectField("name", generatorDescription, "TemporaryGenerator");
                Util.injectField("targetOperation", generatorDescription, operation);
                Util.injectField("initialArrivalTime", generatorDescription, targetTime.getTimeAsDouble());
                Util.injectField("stopTime", generatorDescription, stopTime.getTimeAsDouble());
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

        HookEvent startEvent = new HookEvent(model, data, true, false);
        startEvent.schedule(targetTime);
        HookEvent stopEvent = new HookEvent(model, data, false, false);
        stopEvent.schedule(stopTime);
    }
}