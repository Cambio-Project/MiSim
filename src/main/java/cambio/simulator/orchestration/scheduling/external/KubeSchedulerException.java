package cambio.simulator.orchestration.scheduling.external;

public class KubeSchedulerException extends Exception {

    public KubeSchedulerException(String errorMessage){
        super(errorMessage);
    }
}