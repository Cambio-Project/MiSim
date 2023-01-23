package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * @author Lion Wagner
 * @see AsyncSimpleReportWriter for more information
 */
public class AsyncListReportWriter extends AsyncReportWriter<Object> {
    //we use Object as generic type to move the "toString" conversion
    //to the formatter in the background thread

    private double currentTime = -1;

    protected boolean hasStarted = false;

    public AsyncListReportWriter(Path datasetPath) throws IOException {
        this(datasetPath, "Value");
    }

    public AsyncListReportWriter(Path datasetPath, String header) throws IOException {
        super(datasetPath, new String[] {header});
    }

    @Override
    public void addDataPoint(double time, Object data) {
        if (time != currentTime) {
            if (hasStarted) {
                closeLine();
            }

            startNewLine(time);
            buffer.add(data);
            hasStarted = true;
        } else {
            buffer.add(MiSimReporters.csvSeperator + " " + data);
        }
    }

    @Override
    protected void finalizingTODOs() {
        closeLine();
    }

    @Override
    public Function<Object, String> createFormatter() {
        return Object::toString;
    }

    private void startNewLine(double time) {
        currentTime = time;
        buffer.add(time + MiSimReporters.csvSeperator + " [");
    }

    private void closeLine() {
        buffer.add("]\n");
    }
}
