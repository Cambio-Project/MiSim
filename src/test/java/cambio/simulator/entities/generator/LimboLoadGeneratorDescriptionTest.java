package cambio.simulator.entities.generator;

import java.io.File;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class LimboLoadGeneratorDescriptionTest {

    @Test
    void readsDeformedProfileCorrectly() throws Exception {
        File profile = new File("src/test/resources/limbo_model_deformed.csv");

        String config = "" +
            "{" +
            "\"start\": 20.5," +
            "\"model\": \"" + profile.getAbsolutePath().replace("\\", "/") + "\"" +
            ",\"distribution\": \"spike\"" +
            "}";

        LimboLoadGeneratorDescription description = Utils.getLoadGeneratorDescription(config,
            LimboLoadGeneratorDescription.class);

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 2; j++) {
                double targetTime = description.getNextTimeInstant().getTimeAsDouble();
                Assertions.assertEquals(20.5 + i, targetTime);
                System.out.println(targetTime);
            }
        }
    }

    @RepeatedTest(10)
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    void readsHugeFileInTime() {
        File profile = new File("src/test/resources/limbo_model_huge.csv");

        String config = "" +
            "{" +
            "\"model\": \"" + profile.getAbsolutePath().replace("\\", "/") + "\"" +
            ",\"distribution\": \"spike\"" +
            "}";
        Utils.getLoadGeneratorDescription(config, LimboLoadGeneratorDescription.class);

    }
}