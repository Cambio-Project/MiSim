package cambio.simulator.entities.microservice;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Experiment;
import desmoj.core.simulator.ExternalEvent;
import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import testutils.RandomTieredModel;
import testutils.TestUtils;

class MicroserviceInstanceTest {

    @Test
    void shutDownTest() {
        RandomTieredModel model = new RandomTieredModel("MSTestModel", 3, 3);
        Experiment exp = TestUtils.getExampleExperiment(model, 300);


        final List<MicroserviceInstance> instanceList = new LinkedList<>();


        ExternalEvent instanceCollection = new ExternalEvent(model, "InstanceCollection", false) {
            @Override
            public void eventRoutine() throws SuspendExecution {
                model.getAllMicroservices().forEach(microservice -> {
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
            public void eventRoutine() throws SuspendExecution {
                model.getAllMicroservices().forEach(microservice -> microservice.scaleToInstancesCount(0));
            }
        };
        shutdown.schedule(new TimeInstant(200));

        exp.start();
        exp.finish();

        instanceList.forEach(instance -> Assertions.assertEquals(InstanceState.SHUTDOWN, instance.getState()));

    }
}