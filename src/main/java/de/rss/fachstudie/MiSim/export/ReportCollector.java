package de.rss.fachstudie.MiSim.export;

import desmoj.core.report.ReportManager;
import desmoj.core.report.Reporter;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Report Collector, utilizes desmojs' ReportManger to collect Reporters. Can combine data of multiple {@link
 * MultiDataPointReporter}
 *
 * @author Lion Wagner
 */
public class ReportCollector extends ReportManager {
    private static final ReportCollector instance = new ReportCollector("Main");


    public static ReportCollector getInstance() {
        return instance;
    }


    public ReportCollector(String s) {
        super(s);
    }

    /**
     * Collects (and potentially combines) all results of all registered {@link MultiDataPointReporter}.
     * @return returns the values of all MultiDataPointReporters
     */
    public HashMap<String, TreeMap<Double, Object>> collect_data() {
        //collect_datasets
        HashMap<String, HashMap<Double, Object>> dataSets = new HashMap<>();

        for (Reporter reporter : elements()) {
            if (reporter instanceof MultiDataPointReporter) {
                MultiDataPointReporter dynamic_reporter = (MultiDataPointReporter) reporter;
                HashMap<String, HashMap<Double, ?>> dataSets_ofReporter = dynamic_reporter.getDataSets();
                for (Map.Entry<String, HashMap<Double, ?>> datasets_ofReporter_entry : dataSets_ofReporter.entrySet()) {
                    String current_key = datasets_ofReporter_entry.getKey();
                    HashMap<Double, ?> dataSet_ofReporter = datasets_ofReporter_entry.getValue();

                    HashMap<Double, Object> target_dataSet = dataSets.computeIfAbsent(current_key, key -> new HashMap<>());

                    for (Map.Entry<Double, ?> dataSet_entry : dataSet_ofReporter.entrySet()) {
                        target_dataSet.merge(dataSet_entry.getKey(), dataSet_entry.getValue(), (value1, value2) -> value1);
                    }

                }
            }
        }

        HashMap<String,TreeMap<Double, Object>> output = new HashMap<>();
        dataSets.forEach((name, dataSet) -> output.put(name,new TreeMap<>(dataSet)));
        return output;
    }

    /**
     * Resets the collector and all registered reporters
     */
    public void reset() {//
        this.elements().forEach(reporter -> {
            if (reporter instanceof MultiDataPointReporter) {
                ((MultiDataPointReporter) reporter).reset();
            }
        });
        this.elements().forEach(this::deRegister);
    }

}
