package cambio.simulator.events;

import java.util.HashMap;
import java.util.TreeMap;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.ReportCollector;
import cambio.simulator.export.ReportWriter;
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

    public SimulationEndEvent(MiSimModel model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.model = model;
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        model.getArchitectureModel().getMicroservices().forEach(Microservice::finalizeStatistics);

        model.getExperimentMetaData().markStartOfReport(System.currentTimeMillis());
        triggerReport();
        clReport();
        model.getExperimentMetaData().markEndOfExecution(System.currentTimeMillis());
    }

    private void triggerReport() {
        HashMap<String, TreeMap<Double, Object>> data = ReportCollector.getInstance().collectData();
        TreeMap<String, TreeMap<Double, Object>> sortedData = new TreeMap<>(data);
        ReportWriter.writeReporterCollectorOutput(sortedData,
            model.getExperimentMetaData().getReportLocation());
    }

    private void clReport() {
        ExperimentMetaData metaData = model.getExperimentMetaData();
        System.out.println("\n*** MiSim Report ***");
        System.out.println("Simulation of Architecture: "
            + metaData.getArchitectureDescriptionLocation().getAbsolutePath());
        System.out.println("Executed Experiment:        "
            + metaData.getExperimentDescriptionLocation().getAbsolutePath());
        System.out.println("Setup took:                 " + Util.timeFormat(metaData.getSetupDuration()));
        System.out.println("Experiment took:            " + Util.timeFormat(metaData.getExperimentDuration()));
        System.out.println("Report took:                " + Util.timeFormat(metaData.getReportDuration()));
        System.out.println("Execution took:             " + Util.timeFormat(metaData.getExecutionDuration()));

    }
}
