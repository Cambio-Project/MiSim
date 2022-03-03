package cambio.simulator.orchestration.MiSimSpecific;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.entities.microservice.Microservice;
import cambio.simulator.misc.Priority;
import cambio.simulator.models.MiSimModel;
import cambio.simulator.orchestration.Stats;
import cambio.simulator.orchestration.environment.Container;
import cambio.simulator.orchestration.environment.ContainerState;
import cambio.simulator.orchestration.environment.Pod;
import cambio.simulator.orchestration.environment.PodState;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class StatsMiSimEvent extends NamedExternalEvent {


    public StatsMiSimEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        createScalingStats();
    }


    public void createScalingStats(){
        int time = (int) getModel().presentTime().getTimeAsDouble();

        List<Microservice> microservices = ((MiSimModel)getModel()).getArchitectureModel().getMicroservices().stream().collect(Collectors.toList());
        for (Microservice microservice : microservices) {
            Stats.ScalingRecord scalingRecord = new Stats.ScalingRecord();
            scalingRecord.setTime(time);

            double avg = microservice.getAverageRelativeUtilization();
            scalingRecord.setAvgConsumption(avg);
            scalingRecord.setAmountPods(microservice.getInstancesCount());

            List<Stats.ScalingRecord> scalingRecords = Stats.getInstance().getMicroServiceRecordsMap().get(microservice);
            if (scalingRecords != null) {
                scalingRecords.add(scalingRecord);
            } else {
                ArrayList<Stats.ScalingRecord> scalingRecordList = new ArrayList<>();
                scalingRecordList.add(scalingRecord);
                Stats.getInstance().getMicroServiceRecordsMap().put(microservice, scalingRecordList);
            }
        }
    }


}
