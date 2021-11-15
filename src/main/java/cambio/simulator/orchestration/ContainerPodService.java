package cambio.simulator.orchestration;

import cambio.simulator.entities.microservice.MicroserviceInstance;

public class ContainerPodService {
    private static final ContainerPodService instance = new ContainerPodService();

    //private constructor to avoid client applications to use constructor
    private ContainerPodService(){}

    public static ContainerPodService getInstance(){
        return instance;
    }

    public void putServiceInstanceInContainer(ServiceInstance serviceInstance){
        Container container = new Container(serviceInstance);
        serviceInstance.setContainer(container);

    }

    public Pod createPod(){
        return new Pod();
    }

//    public Container putServiceInstanceInContainer(ServiceInstance serviceInstance){
//        Container container = new Container(serviceInstance);
//    }

    public void putContainerInPod(Container container, Pod pod){
        pod.getContainers().add(container);
//        container.setPod(pod);
    }
}
