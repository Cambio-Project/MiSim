package cambio.simulator.orchestration.events;

import cambio.simulator.entities.NamedExternalEvent;
import cambio.simulator.misc.Priority;
import cambio.simulator.orchestration.k8objects.Deployment;
import cambio.simulator.orchestration.management.ManagementPlane;
import cambio.simulator.orchestration.scheduling.SchedulerType;
import co.paralleluniverse.fibers.SuspendExecution;
import desmoj.core.simulator.Model;
import desmoj.core.simulator.TimeInstant;
import desmoj.core.simulator.TimeSpan;
import org.javatuples.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class CLIEvent extends NamedExternalEvent {

    private static Queue<Pair<TimeSpan, Integer>> cliInformation = new LinkedList<>();

    public CLIEvent(Model model, String name, boolean showInTrace) {
        super(model, name, showInTrace);
        this.setSchedulingPriority(Priority.HIGH);
    }

    @Override
    public void eventRoutine() throws SuspendExecution {
        //changes desired state
        if(cliInformation.peek()==null){
            String message = "No instructions in CLI Queue left";
            System.out.println(message);
            sendTraceNote(message);
            return;
        }

        List<Deployment> deployments = ManagementPlane.getInstance().getDeployments();
        List<Deployment> collect = deployments.stream().filter(deployment -> !deployment.getSchedulerType().equals(SchedulerType.ROUNDROBIN)).collect(Collectors.toList());
        if(collect.size()==1){
            Deployment deployment = collect.get(0);
            Pair<TimeSpan, Integer> poll = cliInformation.poll();
            deployment.setDesiredReplicaCount(poll.getValue1());
            HealthCheckEvent healthCheckEvent = new HealthCheckEvent(getModel(), "HealthCheckEvent - After Scaling", traceIsOn());
            healthCheckEvent.schedule(new TimeSpan(HealthCheckEvent.delay));
            CLIEvent cliEvent = new CLIEvent(getModel(), "CLIEvent", traceIsOn());
            cliEvent.schedule(new TimeSpan(5));
        }else{
            throw new IllegalStateException("There should be only one deployment for the tests");
        }

    }

    public static Queue<Pair<TimeSpan, Integer>> getCliInformation() {
        return cliInformation;
    }

    public static void setCliInformation(Queue<Pair<TimeSpan, Integer>> cliInformation) {
        CLIEvent.cliInformation = cliInformation;
    }
}
