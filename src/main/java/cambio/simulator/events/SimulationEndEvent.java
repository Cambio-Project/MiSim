package cambio.simulator.events;

import java.io.IOException;
import java.util.HashMap;
import java.util.TreeMap;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.ExportUtils;
import cambio.simulator.export.ReportCollector;
import cambio.simulator.export.ReportWriter;
import cambio.simulator.misc.Priority;
import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;

/**
 * A {@link SimulationEndEvent} is an {@link ExternalEvent} that is called upon the end of the simulation.
 *
 * <p>
 * It is used for cleanup and finalizing statistics.
 */
public class SimulationEndEvent extends NamedExternalEvent {

    private final MiSimModel model;

    /**
     * Creates a new {@link SimulationEndEvent} that finishes off the simulation.
     *
     * <p>
     * This Event automatically is assigned {@link Priority#HIGH} so it executes before the {@link
     * desmoj.core.simulator.ExternalEventStop} that stops the simulation.
     */
    public SimulationEndEvent(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.model = model;
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        model.getArchitectureModel().getMicroservices().forEach(Microservice::finalizeStatistics);

        model.getExperimentMetaData().markStartOfReport(System.nanoTime());
        triggerReport();
        model.getExperimentMetaData().markEndOfExecution(System.nanoTime());

        //update the report metadata
        try {
            ExportUtils.updateMetaData(model.getExperimentMetaData());
        } catch (IOException e) {
            System.out.println("[Error] could not write final metadata. The write-out you will find in the results may"
                + " only contains information gathered before the simulation started.");
        }

        writeCommandLineReport();
    }

    private void triggerReport() {
        HashMap<String, TreeMap<Double, Object>> data = ReportCollector.getInstance().collectData();
        TreeMap<String, TreeMap<Double, Object>> sortedData = new TreeMap<>(data);
        ReportWriter.writeReporterCollectorOutput(sortedData,
            model.getExperimentMetaData().getReportLocation());
        ReportCollector.getInstance().reset(); //reset the collector for static usage
    }

    private void writeCommandLineReport() {
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
