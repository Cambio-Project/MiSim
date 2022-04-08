package cambio.simulator.export;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import desmoj.core.report.ReportManager;

/**
 * Report Collector, utilizes desmojs' ReportManger to collect Reporters. Can combine data of multiple {@link
 * MultiDataPointReporter}
 *
 * @author Lion Wagner
 */
public class ReportCollector extends ReportManager {

    //this requires instance to exist, so all static DataPointReporters need to be initialized here
    public static final MultiDataPointReporter USER_REQUEST_REPORTER = new MultiDataPointReporter("R");
    private static final ReportCollector instance = new ReportCollector("Main");


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
    public Map<String, TreeMap<Double, Object>> collectData() {
        //collect_datasets
        Map<String, Map<Double, Object>> dataSets = new ConcurrentHashMap<>();

        elements().stream().parallel()
            .filter(reporter -> reporter instanceof MultiDataPointReporter)
            .map(reporter -> (MultiDataPointReporter) reporter)
            .map(MultiDataPointReporter::getDataSets)
            .flatMap(dataSetsOfReporter -> dataSetsOfReporter.entrySet().stream())
            .forEach(datasetsOfReporterEntry -> {
                String currentKey = datasetsOfReporterEntry.getKey();
                HashMap<Double, ?> dataSetOfReporter = datasetsOfReporterEntry.getValue();
                Map<Double, Object> targetDataSet =
                    dataSets.computeIfAbsent(currentKey, key -> new ConcurrentHashMap<>());
                dataSetOfReporter.entrySet().stream().parallel().forEach(entry -> targetDataSet
                    .merge(entry.getKey(), entry.getValue(), (value1, value2) -> value1));
            });

        Map<String, TreeMap<Double, Object>> output = new ConcurrentHashMap<>();
        dataSets.entrySet().stream().parallel()
            .forEach((entry) -> output.put(entry.getKey(), new TreeMap<>(entry.getValue())));
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

    /**
     * Writes the collected data to the report directory. Also updates the metadata file with the new execution
     * timings.
     *
     * @param model The model that contains the metadata to reference.
     */
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
        Map<String, TreeMap<Double, Object>> data = ReportCollector.getInstance().collectData();
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
