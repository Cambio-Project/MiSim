package cambio.simulator.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
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

    public void printReport(MiSimModel model) {
        model.getExperimentMetaData().markStartOfReport(System.nanoTime());
        sortAndWriteReport(model);
        model.getExperimentMetaData().markEndOfExecution(System.nanoTime());

        //update the report metadata
        try {
            ExportUtils.updateMetaData(model.getExperimentMetaData());
        } catch (IOException e) {
            System.out.println("[Error] could not write final metadata. The write-out you will find in the results may"
                + " only contains information gathered before the simulation started.");
        }

        writeCommandLineReport(model);
    }

    private void sortAndWriteReport(MiSimModel model) {
        HashMap<String, TreeMap<Double, Object>> data = ReportCollector.getInstance().collectData();
        TreeMap<String, TreeMap<Double, Object>> sortedData = new TreeMap<>(data);
        ReportWriter.writeReporterCollectorOutput(sortedData,
            model.getExperimentMetaData().getReportLocation());
        ReportCollector.getInstance().reset(); //reset the collector for static usage
    }

    private void writeCommandLineReport(MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        System.out.println("\n*** MiSim Report ***");
        System.out.println("Simulation of Architecture: "
            + metaData.getArchitectureDescriptionLocation().getAbsolutePath());
        System.out.println("Executed Experiment:        "
            + metaData.getExperimentDescriptionLocation().getAbsolutePath());
        System.out.println("Report Location:            "
            + metaData.getReportLocation().toAbsolutePath());
        System.out.println("Setup took:                 " + Util.timeFormat(metaData.getSetupDuration()));
        System.out.println("Experiment took:            " + Util.timeFormat(metaData.getExperimentDuration()));
        System.out.println("Report took:                " + Util.timeFormat(metaData.getReportDuration()));
        System.out.println("Execution took:             " + Util.timeFormat(metaData.getExecutionDuration()));
    }

}
