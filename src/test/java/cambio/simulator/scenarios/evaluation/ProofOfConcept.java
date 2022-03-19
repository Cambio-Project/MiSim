package cambio.simulator.scenarios.evaluation;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.assertEquals;

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

    static void runSimulation(int exitCode, File arch, File exp, String... additionalArgs) throws Exception {
        int code = catchSystemExit(() -> {
            String[] fileLocations = new String[] {"-a", arch.getAbsolutePath(), "-e", exp.getAbsolutePath()};
            String[] allArgs = new String[additionalArgs.length + 4];
            System.arraycopy(fileLocations, 0, allArgs, 0, fileLocations.length);
            System.arraycopy(additionalArgs, 0, allArgs, 4, additionalArgs.length);
            Main.main(allArgs);
        });
        assertEquals(code, exitCode);
    }


    @Test
    void CircuitBreakerDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_circuitbreaker_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_circuitbreaker_demonstration.json");
        runSimulation(0, arch, exp,  "-d", "-t");
    }

    @Test
    void RetryConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_demonstration.json");
        runSimulation(0, arch, exp,  "-d", "-t");
    }

    @Test
    void RetryDisabledConceptDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_retry_disabled_demonstration.json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_retry_disabled_demonstration.json");
        runSimulation(0, arch, exp,  "-d", "-t");
    }

    @Test
    void LoadBalancerRandomDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_random_demonstration" +
            ".json");
        File exp = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_random_demonstration" +
            ".json");
        runSimulation(0, arch, exp,  "-d", "-t");
    }

    @Test
    void LoadBalancerUtilDemonstration() throws Exception {
        File arch = FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "arch_loadbalancer_util_demonstration" +
            ".json");
        File exp =
            FileLoaderUtil.loadFromExampleResources("ProofOfConcept", "exp_loadbalancer_util_demonstration.json");
        runSimulation(0, arch, exp,  "-d", "-t");
    }
}
