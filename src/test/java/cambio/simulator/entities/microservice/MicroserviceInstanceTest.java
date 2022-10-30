package cambio.simulator.entities.microservice;

import java.lang.reflect.Field;
import java.util.*;

import cambio.simulator.events.SimulationEndEvent;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.*;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.*;
import org.javatuples.Pair;
import org.junit.jupiter.api.*;


@Disabled
class MicroserviceInstanceTest  extends TestBase {

    @Test
    void allInstancesInStateShutdownCorrectly() {
        Pair<MiSimModel, TestExperiment> mockResult = getConnectedMockModel();
        MiSimModel model = mockResult.getValue0();
        Experiment exp = mockResult.getValue1();


        final List<MicroserviceInstance> instanceList = new LinkedList<>();


        ExternalEvent instanceCollection = new ExternalEvent(model, "InstanceCollection", false) {
            @Override
            public void eventRoutine(){
                model.getArchitectureModel().getMicroservices().forEach(microservice -> {
                    try {
                        Field f = Microservice.class.getDeclaredField("instancesSet");
                        f.setAccessible(true);
                        instanceList.addAll((Collection<? extends MicroserviceInstance>) f.get(microservice));
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        e.printStackTrace();
                    }
                });
            }
        };
        instanceCollection.schedule(new TimeInstant(10));

        ExternalEvent shutdown = new ExternalEvent(model, "ShutdownEvent", false) {
            @Override
            public void eventRoutine() {
                model.getArchitectureModel().getMicroservices().forEach(microservice -> microservice.scaleToInstancesCount(0));
            }
        };
        shutdown.schedule(new TimeInstant(10));

        SimulationEndEvent endEvent = new SimulationEndEvent(model, "EndEvent", false);
        endEvent.schedule(new TimeInstant(11));

        exp.start();
        exp.finish();

        instanceList.forEach(instance -> Assertions.assertTrue(
            instance.getState() == InstanceState.SHUTDOWN || instance.getState() == InstanceState.SHUTTING_DOWN));
        instanceList.forEach(instance -> Assertions.assertEquals(0.0, instance.getRelativeWorkDemand()));
    }
}