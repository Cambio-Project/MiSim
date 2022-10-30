package cambio.simulator.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

import org.javatuples.Pair;

/**
 * @author Lion Wagner
 */
public class AsyncSimpleReportWriter extends AsyncReportWriter<Pair<Double, Object>> {

    public AsyncSimpleReportWriter(Path datasetPath) throws IOException {
        super(datasetPath);
    }

    @Override
    public void addDataPoint(double time, Object data) {
        addDataPoint(new Pair<>(time, data));
    }

    public void addDataPoint(Pair<Double, Object> data) {
        buffer.add(data);
    }

    @Override
    public Function<Pair<Double, Object>, String> createFormatter() {
        return pair -> pair.getValue0() + MiSimReporters.csvSeperator + pair.getValue1() + "\n";
    }
}
