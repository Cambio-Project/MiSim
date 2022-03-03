package cambio.simulator.events;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.export.ExportUtils;
import cambio.simulator.export.ReportCollector;
import cambio.simulator.export.ReportWriter;
import cambio.simulator.misc.Priority;
import cambio.simulator.misc.Util;
import cambio.simulator.models.ExperimentMetaData;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.k8objects.Deployment;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.ExternalEvent;

import static java.nio.file.Files.*;

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

        clReport();

        String currentRunName = "A1";
        createOrchestrationReport(currentRunName);
    }

    public void createOrchestrationReport(String currentRunName) {
        //Create orchestration record directory

        String directoryName = "orchestration_records";
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        File directoryScalingSpecificRun = new File(directory.getPath() + "/" + currentRunName);
        if (!directoryScalingSpecificRun.exists()) {
            directoryScalingSpecificRun.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        //Create Data for scaling

        String directoryNameScaling = "Scaling";
        File directoryScaling = new File(directoryScalingSpecificRun.getPath() + "/" + directoryNameScaling);
        if (!directoryScaling.exists()) {
            directoryScaling.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }


        try {
            copyDirectory("orchestration", directoryScalingSpecificRun.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<Deployment, List<Stats.ScalingRecord>> deploymentRecordsMap = Stats.getInstance().getDeploymentRecordsMap();

        for (Deployment deployment : deploymentRecordsMap.keySet()) {
            List<Stats.ScalingRecord> scalingRecords = deploymentRecordsMap.get(deployment);
            File csvOutputFile = new File(directoryScaling.getPath() + "/" + deployment.getPlainName() + ".csv");
            try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
                ArrayList<String> content = new ArrayList<>();
                content.add("Time");
                content.add("AvgConsumption");
                content.add("#Pods");
                pw.println(convertToCSV(content));
                for (Stats.ScalingRecord scalingRecord : scalingRecords) {
                    content.clear();
                    content.add(String.valueOf(scalingRecord.getTime()));
                    content.add(String.valueOf(scalingRecord.getAvgConsumption()));
                    content.add(String.valueOf(scalingRecord.getAmountPods()));
                    pw.println(convertToCSV(content));
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copyDirectory(String sourceDirectoryLocation, String destinationDirectoryLocation)
            throws IOException {
        walk(Paths.get(sourceDirectoryLocation))
                .forEach(source -> {
                    Path destination = Paths.get(destinationDirectoryLocation, source.toString()
                            .substring(sourceDirectoryLocation.length()));
                    try {
                        copy(source, destination);
                    } catch (IOException e) {
//                        e.printStackTrace();
                    }
                });
    }

    public String convertToCSV(List<String> data) {
        return data.stream()
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
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
        System.out.println("Report Location:            "
                + metaData.getReportLocation().toAbsolutePath());
        System.out.println("Setup took:                 " + Util.timeFormat(metaData.getSetupDuration()));
        System.out.println("Experiment took:            " + Util.timeFormat(metaData.getExperimentDuration()));
        System.out.println("Report took:                " + Util.timeFormat(metaData.getReportDuration()));
        System.out.println("Execution took:             " + Util.timeFormat(metaData.getExecutionDuration()));
    }
}
