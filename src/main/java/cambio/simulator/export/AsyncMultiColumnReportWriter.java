package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import org.javatuples.Pair;

/**
 * Writes a list of values to multiple columns, seperated by {@link MiSimReporters#csvSeperator}.
 *
 * @author Lion Wagner
 */
public class AsyncMultiColumnReportWriter extends AsyncReportWriter<Pair<Double, Iterable<?>>> {

    public AsyncMultiColumnReportWriter(Path datasetPath) throws IOException {
        this(datasetPath, MiSimReporters.DEFAULT_VALUE_COLUMN_NAME);
    }

    public AsyncMultiColumnReportWriter(Path datasetPath, String... headers) throws IOException {
        super(datasetPath, headers);
    }

    @Override
    public void addDataPoint(double time, Object data) {
        if (data instanceof Object[]) {
            Object[] array = (Object[]) data;
            List<Object> list = new ArrayList<>(Arrays.asList(array));
            buffer.add(new Pair<>(time, list));
        } else {
            buffer.add(new Pair<>(time, Collections.singletonList(data)));
        }
    }

    public void addDataPoint(double time, Iterable<?> data) {
        buffer.add(new Pair<>(time, data));
    }

    public void addDataPoint(double time, int[] data) {
        addDataPoint(time, Arrays.stream(data).boxed().toArray());
    }

    public void addDataPoint(double time, double[] data) {
        addDataPoint(time, Arrays.stream(data).boxed().toArray());
    }

    public void addDataPoint(double time, long[] data) {
        addDataPoint(time, Arrays.stream(data).boxed().toArray());
    }

    public void addDataPoint(double time, float[] data) {
        List<Float> list = new ArrayList<>();
        for (float f : data) {
            list.add(f);
        }
        addDataPoint(time, list);
    }

    public void addDataPoint(double time, boolean[] data) {
        List<Boolean> list = new ArrayList<>();
        for (boolean f : data) {
            list.add(f);
        }
        addDataPoint(time, list);
    }

    public void addDataPoint(double time, char[] data) {
        List<Character> list = new ArrayList<>();
        for (char f : data) {
            list.add(f);
        }
        addDataPoint(time, list);
    }

    public void addDataPoint(double time, short[] data) {
        List<Short> list = new ArrayList<>();
        for (short f : data) {
            list.add(f);
        }
        addDataPoint(time, list);
    }

    public void addDataPoint(double time, byte[] data) {
        List<Byte> list = new ArrayList<>();
        for (byte f : data) {
            list.add(f);
        }
        addDataPoint(time, list);
    }


    @Override
    public Function<Pair<Double, Iterable<?>>, String> createFormatter() {
        return pair -> {
            StringBuilder builder = new StringBuilder(String.valueOf(pair.getValue0()));
            for (Object o : pair.getValue1()) {
                builder.append(MiSimReporters.csvSeperator).append(o);
            }
            builder.append("\n");
            return builder.toString();
        };
    }
}
