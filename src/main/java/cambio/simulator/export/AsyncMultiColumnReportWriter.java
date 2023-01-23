package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import org.javatuples.Pair;

/**
 * @author Lion Wagner
 */
public class AsyncMultiColumnReportWriter extends AsyncReportWriter<Pair<Double, Iterable<?>>> {

    public AsyncMultiColumnReportWriter(Path datasetPath, String[] headers) throws IOException {
        super(datasetPath, headers);
    }

    @Override
    public void addDataPoint(double time, Object data) {
        if (data instanceof Iterable) {
            buffer.add(new Pair<>(time, (Iterable<?>) data));
        } else {
            throw new IllegalArgumentException("Data must be iterable");
        }
    }

    @Override
    public Function<Pair<Double, Iterable<?>>, String> createFormatter() {
        return pair -> {
            StringBuilder builder = new StringBuilder();
            builder.append(pair.getValue0());
            for (Object o : pair.getValue1()) {
                builder.append(MiSimReporters.csvSeperator).append(o);
            }
            builder.append("\n");
            return builder.toString();
        };
    }
}
