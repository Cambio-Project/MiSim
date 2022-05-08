package cambio.simulator.scenarios.evaluation;

import java.io.File;

import cambio.simulator.test.FileLoaderUtil;
import cambio.simulator.test.TestBase;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Class used to demonstrate the different features of miSim in its inaugural paper.
 *
 * @author Lion Wagner
 */
//@Execution(ExecutionMode.CONCURRENT), not working yet
@Disabled
public class ProofOfConcept extends TestBase {


    @Test
    void CircuitBreakerDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_circuitbreaker_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_circuitbreaker_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void RetryConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void RetryDisabledConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_disabled_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_disabled_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void LoadBalancerRandomDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_random_demonstration" +
            ".json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_random_demonstration" +
            ".json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void LoadBalancerUtilDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_util_demonstration" +
            ".json");
        File exp =
            FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_util_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void LoadBalancerStrictRoundRobinDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_round" +
            "-robin-strict_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_round" +
            "-robin-strict_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }

    @Test
    void LoadBalancerRoundRobinDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_round" +
            "-robin_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_round" +
            "-robin_demonstration.json");
        runSimulationCheckExit(0, arch, exp, "-d", "-t");
    }
}
