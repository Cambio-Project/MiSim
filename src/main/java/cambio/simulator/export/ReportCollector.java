package cambio.simulator.export;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import desmoj.core.report.ReportManager;
import desmoj.core.report.Reporter;

/**
 * Report Collector, utilizes desmojs' ReportManger to collect Reporters. Can combine data of multiple {@link
 * MultiDataPointReporter}
 *
 * @author Lion Wagner
 */
public class ReportCollector extends ReportManager {

    private static final ReportCollector instance = new ReportCollector("Main");

    //this requires instance to exist, so all static DataPointReporters need to be initialized here
    public static final MultiDataPointReporter USER_REQUEST_REPORTER = new MultiDataPointReporter("R");


    public ReportCollector(String name) {
        super(name);
    }

    public static ReportCollector getInstance() {
        return instance;
    }

    /**
     * Collects (and potentially combines) all results of all registered {@link MultiDataPointReporter}.
     *
     * @return returns the values of all MultiDataPointReporters
     */
    public HashMap<String, TreeMap<Double, Object>> collectData() {
        //collect_datasets
        HashMap<String, HashMap<Double, Object>> dataSets = new HashMap<>();

        for (Reporter reporter : elements()) {
            if (reporter instanceof MultiDataPointReporter) {
                MultiDataPointReporter castReporter = (MultiDataPointReporter) reporter;
                HashMap<String, HashMap<Double, ?>> dataSetsOfReporter = castReporter.getDataSets();
                for (Map.Entry<String, HashMap<Double, ?>> datasetsOfReporterEntry : dataSetsOfReporter.entrySet()) {
                    String currentKey = datasetsOfReporterEntry.getKey();
                    HashMap<Double, ?> dataSetOfReporter = datasetsOfReporterEntry.getValue();

                    HashMap<Double, Object> targetDataSet =
                        dataSets.computeIfAbsent(currentKey, key -> new HashMap<>());

                    for (Map.Entry<Double, ?> datasetEntry : dataSetOfReporter.entrySet()) {
                        targetDataSet
                            .merge(datasetEntry.getKey(), datasetEntry.getValue(), (value1, value2) -> value1);
                    }

                }
            }
        }

        HashMap<String, TreeMap<Double, Object>> output = new HashMap<>();
        dataSets.forEach((name, dataSet) -> output.put(name, new TreeMap<>(dataSet)));
        return output;
    }

    /**
     * Resets the collector and all registered reporters.
     */
    public void reset() {
        this.elements().forEach(reporter -> {
            if (reporter instanceof MultiDataPointReporter) {
                ((MultiDataPointReporter) reporter).reset();
            }
            this.deRegister(reporter);
        });
        this.register(USER_REQUEST_REPORTER);
    }
}
