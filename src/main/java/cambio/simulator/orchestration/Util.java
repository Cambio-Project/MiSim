package cambio.simulator.orchestration;

import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.FirstFitScheduler;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Util {

    private static final Util instance = new Util();

    //private constructor to avoid client applications to use constructor
    private Util() {}

    public static Util getInstance() {
        return instance;
    }

    public Set<String> listFilesUsingJavaIO(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }
}
