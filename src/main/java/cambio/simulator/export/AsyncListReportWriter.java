package cambio.simulator.export;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * A {@link AsyncReportWriter} that writes a list of values to a single column. The list is formatted as
 * [entry1,entry2].
 * The list/row is closed and a new one started when a new time is given.
 *
 * <p>
 * Example:
 * <table>
 *     <caption>AsyncListReportWriter Example</caption>
 *     <tr>
 *         <th>SimulationTime</th>
 *         <th>Value</th>
 *     </tr>
 *     <tr>
 *         <td>0.0</td>
 *         <td>[1,2,3]</td>
 *     </tr>
 *     <tr>
 *         <td>0.1</td>
 *         <td>[4,5,6]</td>
 *     </tr>
 * </table>
 *
 * @author Lion Wagner
 * @see AsyncReportWriter
 */
public class AsyncListReportWriter extends AsyncReportWriter<Object> {
    //we use Object as generic type to move the "toString" conversion
    //to the formatter in the background thread

    private double currentTime = -1;

    protected boolean hasStarted = false;

    public AsyncListReportWriter(Path datasetPath) throws IOException {
        this(datasetPath, MiSimReporters.DEFAULT_VALUE_COLUMN_NAME);
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
            buffer.add(MiSimReporters.csvListSeparator + data);
        }
    }

    @Override
    protected void finalizingTodos() {
        closeLine();
    }

    @Override
    public Function<Object, String> createFormatter() {
        return Object::toString;
    }

    private void startNewLine(double time) {
        currentTime = time;
        buffer.add(time + MiSimReporters.csvSeperator + "[");
    }

    private void closeLine() {
        buffer.add("]\n");
    }
}
