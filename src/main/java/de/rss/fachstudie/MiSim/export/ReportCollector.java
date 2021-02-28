package de.rss.fachstudie.MiSim.export;

import desmoj.core.report.ReportManager;
import desmoj.core.report.Reporter;
import desmoj.core.simulator.TimeInstant;

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
     */
    public HashMap<String, TreeMap<TimeInstant, Object>> collect_data() {
        //collect_datasets
        HashMap<String, TreeMap<TimeInstant, Object>> dataSets = new HashMap<>();

        for (Reporter reporter : elements()) {
            if (reporter instanceof MultiDataPointReporter) {
                MultiDataPointReporter dynamic_reporter = (MultiDataPointReporter) reporter;
                HashMap<String, TreeMap<TimeInstant, ?>> dataSets_ofReporter = dynamic_reporter.getDataSets();
                for (Map.Entry<String, TreeMap<TimeInstant, ?>> datasets_ofReporter_entry : dataSets_ofReporter.entrySet()) {
                    String current_key = datasets_ofReporter_entry.getKey();
                    TreeMap<TimeInstant, ?> dataSet_ofReporter = datasets_ofReporter_entry.getValue();

                    TreeMap<TimeInstant, Object> target_dataSet = dataSets.computeIfAbsent(current_key, key -> new TreeMap<>());

                    for (Map.Entry<TimeInstant, ?> dataSet_entry : dataSet_ofReporter.entrySet()) {
                        target_dataSet.merge(dataSet_entry.getKey(), dataSet_entry.getValue(), (value1, value2) -> value1);
                    }

                }
            }
        }
        return dataSets;
    }

}
