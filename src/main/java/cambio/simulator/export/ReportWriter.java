package cambio.simulator.export;

import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 * Output handler of the collected result data.
 *
 * @author Lion Wagner
 */
public class ReportWriter {

    /**
     * writes data to a csv file.
     */
    public static void writeReporterCollectorOutput(TreeMap<String, TreeMap<Double, Object>> data,
                                                    Path reportLocation) {
        //TODO: custom names for value column at CSVExporter#writeDataset(String,String,Map)
        data.entrySet().parallelStream()
            .forEach(dataset -> CSVExporter.writeDataset(dataset.getKey(), dataset.getValue(), reportLocation));
    }

}
