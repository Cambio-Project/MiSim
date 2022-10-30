package cambio.simulator.entities.patterns;

import cambio.simulator.models.MiSimModel;
import cambio.simulator.test.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CircuitBreakerTest extends TestBase {

    @Test
    void test_has_lower_priority_then_retry() {
        MiSimModel mockModel = getConnectedMockModel().getValue0();

        CircuitBreaker cb = new CircuitBreaker(mockModel, "", false);
        Retry retry = new Retry(mockModel, "", false);

        Assertions.assertTrue(cb.getListeningPriority() < retry.getListeningPriority());

    }
}