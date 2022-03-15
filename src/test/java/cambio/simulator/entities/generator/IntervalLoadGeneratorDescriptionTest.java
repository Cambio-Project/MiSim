package cambio.simulator.entities.generator;

import java.util.Random;

import desmoj.core.simulator.TimeInstant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IntervalLoadGeneratorDescriptionTest {

    @Test
    void generatesEvenCorrectly() throws LoadGeneratorStopException {
        String config = "" +
            "{" +
            "\"interval\": 2," +
            "\"load\": 4," +
            "\"start\": 20.5" +
            "}";

        IntervalLoadGeneratorDescription description = Utils.getLoadGeneratorDescription(config,
            IntervalLoadGeneratorDescription.class);

        for (int i = 0; i < 100; i++) {
            TimeInstant next = description.getNextTimeInstant(new TimeInstant(i));
            Assertions.assertEquals(20.5 + .5 * i, next.getTimeAsDouble());
        }
    }

    @Test
    void generatesSpikeCorrectly() throws LoadGeneratorStopException {
        String config = "" +
            "{" +
            "\"interval\": 2," +
            "\"load\": 4," +
            "\"start\": 20.5," +
            "\"distribution\": \"spike\"" +
            "}";

        IntervalLoadGeneratorDescription description = Utils.getLoadGeneratorDescription(config,
            IntervalLoadGeneratorDescription.class);

        for (int i = 0; i < 100; i++) {
            for (int j = 0; j < 4; j++) {
                TimeInstant next = description.getNextTimeInstant(new TimeInstant(j+i*4));
                Assertions.assertEquals(20.5 + 2 * i, next.getTimeAsDouble());
            }
        }
    }

    @Test
    void doesNotStartOnNaNInterval() {
        String config = "" +
            "{" +
            "\"interval\": NaN" +
            "}";

        IntervalLoadGeneratorDescription description = Utils.getLoadGeneratorDescription(config,
            IntervalLoadGeneratorDescription.class);
        Assertions.assertThrows(LoadGeneratorStopException.class, () -> description.getNextTimeInstant(new TimeInstant(0)));
    }

    @Test
    void throwsExceptionOn0Interval() {
        String config = "" +
            "{" +
            "\"interval\": 0" +
            "}";
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Utils.getLoadGeneratorDescription(config, IntervalLoadGeneratorDescription.class));
    }

    @Test
    void throwsExceptionOnNegativeInterval() {
        Random rng = new Random();
        for (int i = 0; i < 1000; i++) {
            String config = "" +
                "{" +
                "\"interval\": -" + rng.nextInt() +
                "}";
            Assertions.assertThrows(IllegalArgumentException.class,
                () -> Utils.getLoadGeneratorDescription(config, IntervalLoadGeneratorDescription.class));
        }
    }
}