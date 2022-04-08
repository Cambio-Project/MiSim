package cambio.simulator.scenarios.evaluation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
//@Execution(ExecutionMode.CONCURRENT), not working yet
public class ProofOfConcept extends TestBase {


    @Test
    void CircuitBreakerDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_circuitbreaker_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_circuitbreaker_demonstration.json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }

    @Test
    void RetryConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_demonstration.json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }

    @Test
    void RetryDisabledConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_disabled_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_disabled_demonstration.json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }

    @Test
    void LoadBalancerRandomDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_random_demonstration" +
            ".json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_random_demonstration" +
            ".json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }

    @Test
    void LoadBalancerUtilDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_util_demonstration" +
            ".json");
        File exp =
            FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_util_demonstration.json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }

    @Test
    void LoadBalancerRoundRobinDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_round" +
            "-robin_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_round" +
            "-robin_demonstration.json");
        runSimulationCheckExit(0, arch, exp,  "-d", "-t");
    }
}
