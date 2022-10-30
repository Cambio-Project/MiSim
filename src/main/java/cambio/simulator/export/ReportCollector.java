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

    private static ReportCollector instance;


    public ReportCollector(String name) {
        super(name);
    }

    public static ReportCollector getInstance() {
        return (instance == null) ? instance = new ReportCollector("Main") : instance;
    }


    /**
     * Writes the collected data to the report directory. Also updates the metadata file with the new execution
     * timings.
     *
     * @param model The model that contains the metadata to reference.
     */
    public void printReport(MiSimModel model) {
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

    private void writeCommandLineReport(MiSimModel model) {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        System.out.println("\n*** MiSim Report ***");
        System.out.println("Simulation of Architecture: "
            + metaData.getArchitectureDescriptionLocation().getAbsolutePath());
        System.out.println("Executed Experiment:        "
            + metaData.getExperimentDescriptionLocation().getAbsolutePath());
        System.out.println("Report Location:            "
            + metaData.getReportLocation().toAbsolutePath().normalize());
        System.out.println("Parsing took:                 " + Util.timeFormat(metaData.getSetupExecutionDuration()));
        System.out.println("Experiment took:            " + Util.timeFormat(metaData.getExperimentExecutionDuration()));
//        System.out.println("Report took:                " + Util.timeFormat(metaData.getReportExecutionDuration()));
        System.out.println("Execution took:             " + Util.timeFormat(metaData.getExecutionDuration()));
    }

}
