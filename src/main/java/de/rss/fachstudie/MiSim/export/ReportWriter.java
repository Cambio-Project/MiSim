package de.rss.fachstudie.MiSim.export;

import desmoj.core.simulator.TimeInstant;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lion Wagner
 */
public class ReportWriter {

    public static void writeReporterCollectorOutput(TreeMap<String, TreeMap<TimeInstant, Object>> data) {
        for (Map.Entry<String, TreeMap<TimeInstant, Object>> dataset : data.entrySet()) {
            CSVExporter.writeDataset(dataset.getKey(), dataset.getValue());
            //TODO: custom names for value column at CSVExporter#writeDataset(String,String,Map)
        }
    }

}
