package cambio.simulator.entities.patterns;

import static cambio.simulator.test.FileLoaderUtil.loadFromTestResources;

import java.io.File;

import cambio.simulator.Main;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class RetryTest {
    @Test
    void initialFail() {
        File arch = loadFromTestResources("test_architecture.json");
        File experiment = loadFromTestResources("all_timeout_experiment.json");
        Main.main(new String[] {"-a ", arch.getAbsolutePath(), " -e ", experiment.getAbsolutePath(), "-t"});
    }
}