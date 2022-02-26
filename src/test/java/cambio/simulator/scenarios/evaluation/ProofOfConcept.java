package cambio.simulator.scenarios.evaluation;

import java.io.File;

import cambio.simulator.Main;
import cambio.simulator.testutils.FileLoaderUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Lion Wagner
 */
@Disabled
//@Execution(ExecutionMode.CONCURRENT), not working yet
public class ProofOfConcept {

    @Test
    void AutoScaleDemonstration() {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_autoscale_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_autoscale_demonstration.json");
        String[] args = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-t"};
        Main.main(args);
    }

    @Test
    void CircuitBreakerDemonstration() {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_circuitbreaker_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_circuitbreaker_demonstration.json");
        String[] args = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-t"};
        Main.main(args);
    }

    @Test
    void RetryConceptDemonstration() {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_demonstration.json");
        String[] args = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-t"};
        Main.main(args);
    }

    @Test
    void LoadBalancerRandomDemonstration() {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_random_demonstration" +
            ".json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_demonstration" +
            ".json");
        String[] args = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-t"};
        Main.main(args);
    }

    @Test
    void LoadBalancerUtilDemonstration() {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_util_demonstration" +
            ".json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_demonstration" +
            ".json");
        String[] args = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath(), "-t"};
        Main.main(args);
    }
}
