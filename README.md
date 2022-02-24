[![Build Artifact](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml/badge.svg)](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml)
[![Documentation](https://img.shields.io/badge/Documentation-online-32CA55?style=flat&logo=github&logoColor=959DA5&labelColor=2F353C)](https://Cambio-Project.github.io/resilience-simulator/)
[![DOI](https://zenodo.org/badge/200825938.svg)](https://zenodo.org/badge/latestdoi/200825938)

## WIP Note: This ReadMe is still WIP. Some information is not true for MiSim 3.0 onwards.

# MiSim - Microservice Resilience Simulator

This simulator was created as part of two Bachelor Thesis and a Fachstudie __Simulation-based Resilience Prediction of
Microservice Architectures__ at the Software Quality Group of the Institute of Software Engineering at the University of
Stuttgart.

It allows the simulation of microservice architectures in regard to resilience and is based on
the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation.

**Table of contents:**

- [Installation](#Installation)
- [Execution](#Execution)
- [Architectural Model](#arch_mod)
- [Experiment Model](#exp_mod)
- [Orchestration](#orchestration)

## <a name="Usage"></a>Usage

### Artifact Download

Download the [newest stable release](https://github.com/Cambio-Project/resilience-simulator/releases) or
a [nightly build](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml) (see
artifacts of newest workflow run).

### Build from source

Clone via git and run
`mvn -B package --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"`. You should
see a `misim.jar` file in the resulting `target/` directory.

### Execution

Simply run `java -jar misim.jar [arguments]`.

### Parameters

| Argument | Short | Required | Description | Example |
|----------|------|----------|-------------|---------|
|   --arch_model       |   -a   |     true     |    provides path to the architecture file         |     ./Examples/example_architecture_scaling.json    |

## <a name="Execution"></a>Execution

The simulation works only when the relative path `./Report` exists in execution directory. With the following file
structure ...

```
project/
|--- Examples/
    |--- architecture_model.json
    |--- experiment_model.json
    |--- ...
|--- Report/
    |--- css/
    |--- js/
    |--- ...
|--- MiSim.jar
|--- ...
```

... use the following command to run a simulation:

`java -jar MiSim.jar -a ./Examples/architecture_model.json -e ./Examples/experiment_model.json -p`

## <a name="arch_mod"></a>Architectural Model

The architectural model is required as input for the simulator. It is saved in a _JSON_ file. The following is a simple
example for the architectural model:

```json
{
  "microservices": [
    {
      "name": "B",
      "instances": 2,
      "patterns": [],
      "capacity": 1000,
      "operations": [
        {
          "name": "b1",
          "demand": 160,
          "circuitBreaker": {
            "rollingWindow": 10,
            "requestVolumeThreshold": 4,
            "errorThresholdPercentage": 0.5,
            "sleepWindow": 5,
            "timeout": 1
          },
          "dependencies": [
            {
              "service": "D",
              "operation": "d1",
              "probability": 1.0
            },
            {
              "service": "E",
              "operation": "e1",
              "probability": 1.0
            }
          ]
        }
      ]
    },
    {
      "name": "A",
      "instances": 2,
      "patterns": [],
      "capacity": 1000,
      "operations": [
        {
          "name": "a2",
          "demand": 120,
          "circuitBreaker": null,
          "dependencies": [
            {
              "service": "C",
              "operation": "c2",
              "probability": 1.0
            }
          ]
        },
        {
          "name": "a1",
          "demand": 140,
          "circuitBreaker": null,
          "dependencies": [
            {
              "service": "B",
              "operation": "b1",
              "probability": 1.0
            },
            {
              "service": "C",
              "operation": "c1",
              "probability": 1.0
            }
          ]
        }
      ]
    },
    {
      "name": "D",
      "instances": 1,
      "patterns": [],
      "capacity": 1000,
      "operations": [
        {
          "name": "d1",
          "demand": 170,
          "circuitBreaker": null,
          "dependencies": []
        }
      ]
    },
    {
      "name": "E",
      "instances": 2,
      "patterns": [],
      "capacity": 1000,
      "operations": [
        {
          "name": "e2",
          "demand": 180,
          "circuitBreaker": null,
          "dependencies": []
        },
        {
          "name": "e1",
          "demand": 430,
          "circuitBreaker": null,
          "dependencies": []
        }
      ]
    },
    {
      "name": "C",
      "instances": 1,
      "patterns": [],
      "capacity": 1000,
      "operations": [
        {
          "name": "c1",
          "demand": 192,
          "circuitBreaker": {
            "rollingWindow": 10,
            "requestVolumeThreshold": 4,
            "errorThresholdPercentage": 0.5,
            "sleepWindow": 5,
            "timeout": 1
          },
          "dependencies": [
            {
              "service": "E",
              "operation": "e2",
              "probability": 1.0
            }
          ]
        },
        {
          "name": "c2",
          "demand": 90,
          "circuitBreaker": null,
          "dependencies": []
        }
      ]
    }
  ]
}

```

### Description

The model contains architectural information about the microservices of the system, their operations and dependencies
and the resilience patterns they implement.

- ___name___: Name of the microservice
- ___instances___: Number of instances of this microservice
- ___capacity___: CPU capacity of _each_ instance in Mhz
- ___patterns___: Array of resilience patterns that are implemented in this microservice. The array contains objects
  which hold information about the respective resilience pattern
    - ___name___: The name of the pattern. As of now the only supported pattern is _Resource Limiter_
    - ___arguments___: An array which contains parameters about the pattern
- ___operations___: Array which holds objects which contain information about the different operations that this
  microservice can perform
    - ___name___: Name of the operation
    - ___demand___: CPU demand of this operation in Mhz
    - ___circuitBreaker___: Contains the following parameters that configure the implemented circuit breaker: *
      rollingWindow*, *requestVolumeThreshold*, *errorThresholdPercentage*, *sleepWindow* and *timeout*. If the
      operations doesn't implement a circuit breaker the value is *null*
    - ___dependencies___: Arry of objects which hold information about a dependency that this operation has with another
      operation.
        - ___service___: Name of the microservice to which the other operation belongs
        - ___operation___: Name of the other operation from which this operation depends
        - ___probability___: The probability that this operation will call the other operation (decimal number in
          between 0 and 1)

## <a name="exp_mod"></a>Experiment Model

The experiment model contains meta information for the simulation and information about the experiment.

This is an example for the experiment model:

```json
{
  "simulation_meta_data": {
    "experiment_name": "ABCDE Experiment",
    "model_name": "Schema ABCDE",
    "duration": 50,
    "report": "",
    "datapoints": 50,
    "seed": 979
  },
  "request_generators": [
    {
      "microservice": "A",
      "operation": "a1",
      "interval": 0.25
    },
    {
      "microservice": "A",
      "operation": "a2",
      "interval": 0.25
    }
  ],
  "chaosmonkeys": [
    {
      "microservice": "E",
      "instances": 1,
      "time": 10
    }
  ]
}
```

### Simulation Meta Data

The _simulation-meta-data_ object holds meta data that's needed for the simulation.

- ___experiment_name___: Name of the experiment
- ___model_name___: Name of the used model
- ___duration___: The duration of the experiment in seconds, must be an integer
- ___report___: The simulator creates a report at the end of the simulation. Leave this field empty if you want a
  detailed report, set the value to "minimalistic" if you want a minimalistic version of the report or set it to "none"
  if you don't want a report
- ___datapoints___: The number of datapoints you want for the charts in your report. The simulator records statistics at
  every datapoint. If you set the value to "0" no charts will be created. If you set it to "-1" the simulator will
  record a datapoint at every simulated second
- ___seed___: A seed for the randomly generated events in the simulator. Leave this field empty if you want random
  experiments or set the value to an integer to use a seed

### Request Generators

The _request_generators_ array holds objects which contain information about the generation of inital requests to
different microservices of the system to start the simulation.

- ___service___: Name of the microservice to which the request should be send
- ___operation___: Name of the operation which should be performed
- ___interval___: Time interval in seconds in which these requests will be created

### Chaosmonkeys

The _chaosmonkeys_ array holds objects which contain information about chaos monkeys which shut down instances of
specified microservices during the simulation.

- ___service___: Name of the service of which you want to shut down a number of instances during the simulation
- ___instances___: Number of instances you want to shut down during the simulation
- ___time___: Time point (in seconds) at which you want to shut down the instances of the specified microservie

## <a name="orchestration"></a>Orchestration
The orchestration plug in enables MiSim to support container orchestration tasks. It is enabled by...


###Architecture File
The architecture file is still necessary to run MiSim. 
It did not change but supports new values for the field 
- ___loadbalancer_strategy___:  [___leastUtil_orchestration___, ___random_orchestration___]


```json
{
  "microservices": [
    {
      "name": "users",
      "instances": 3,
      "loadbalancer_strategy": "leastUtil_orchestration",
      "patterns": [],
      "capacity": 5,
      "operations": [...]
    }
}
```

###<a name="experiment_orchestration"></a>Experiment File
The experiment file is extended. It needs the following fields for enabling orchestration mode
- ___orchestrate___: true/false - 
- ___orchestration_dir___: <<folder_path>> - Insert here the relative folder path, where your config and yaml files are located. 
The folder needs to bear the subdirectories, called ___environment___ (config.yaml inside), and ___k8_files___ (k8 yaml files inside). 

Additionally, the user can configure **Chaos Monkey for pods** similar to the usual Chaos Monkeys.
Instead of giving a ___microservice___ the user needs to insert a ___deployment___ by providing the corresponding name.
```json
{
  "simulation_meta_data": {
    "experiment_name": "ABCDE Experiment",
    "model_name": "architecture_model",
    "orchestrate": true,
    "orchestration_dir": "orchestration",
    "duration": 60,
    "report": "",
    "datapoints": 20,
    "seed": 979
  },
  "request_generators": [
    {
      "type": "interval",
      "config": {
        "microservice": "frontend",
        "operation": "createBook",
        "interval": 5
      }
    },
    {
      "type": "interval",
      "config": {
        "microservice": "books",
        "operation": "books.GET",
        "interval": 10
      }
    }
  ],

  "chaos_monkeys": [
    {
      "type": "monkey",
      "config":     {
        "microservice": "books",
        "instances": 1,
        "time": 5
      }
    },
    {
      "type": "monkey",
      "config":     {
        "microservice": "books",
        "instances": 1,
        "time": 20
      }
    }
  ],

  "chaos_monkeys_pods": [
    {
      "type": "monkey_pods",
      "config":     {
        "deployment": "frontend-deployment",
        "instances": 1,
        "time": 30
      }
    }
  ]
}
```

###The orchestration files
The orchestration files need to be placed in the orchestration folder that was explicitly specified in the 
experiment file (see [Experiment File](#experiment_orchestration)).

The project structure looks like this:

```
project/
|--- orchestration/
    |--- environment/
        |--- config.yaml
    |--- k8_files
        |--- books-deployment.yaml
        |--- hpa-books.yaml
|--- Examples/
    |--- architecture_model.json
    |--- experiment_model.json
    |--- ...
|--- Report/
    |--- css/
    |--- js/
    |--- ...
|--- MiSim.jar
|--- ...
```

#### config.yaml
A file called **config.yaml** needs to be provided inside the folder ___environment___:

```yaml
nodes:
  amount: 1
  cpu: 10
customNodes:
  - name: Test5Node
    cpu: 5
  - name: Test6Node
    cpu: 6
  - name: Large
    cpu: 10
scaler:
  holdTimeUpScaler: 5
  holdTimeDownScaler: 3
loadBalancer: random_orchestration
scheduler: firstFit
schedulerPrio:
  - name: kube
    prio: 2
  - name: firstFit
    prio: 3
```

The config.yaml contains information necessary for the orchestration process:

- ___nodes___: Info about homogenous nodes 
  - ___amount___: Number of nodes in the cluster
  - ___cpu___: CPU capacity of each node in Mhz
- ___customNodes___: Possibility to add nodes with specified attributes
    - ___name___: The name of the node
    - ___cpu___: CPU capacity of this node in Mhz
- ___scaler___: Holds information about scaling restrictions
    - ___holdTimeUpScaler___: Time when upscaling is allowed in seconds after the last upscaling event
    - ___holdTimeDownScaler___: Time when downscaling is allowed in seconds after the last downscaling event
- ___loadBalancer___: Default loadbalancer that is used when not given in architecture file
  (possible values: ___leastUtil_orchestration___, ___random_orchestration___)
- ___scheduler___: Default scheduler that is used when not given in deployment file
  (possible values: ___firstFit___, ___random___, ___kube___)
- ___schedulerPrio___: Defines the order of scheduling during the simulation
- ___name___: Name of the scheduler
- ___prio___: Prio of the scheduler (lower numbers before higher ones)


#### k8 files
In this directory, deployment.yaml and corresponding Horizontal Pod Autoscalers (HPA) can be defined. However, 
none of these files are necessary to run the simulation. The simulation can also run with only the architecture file
given. The deployment logic looks like this:

 - if service is specified in k8s deployments and in architecture model -> one deployment created
 - if service is specified in k8s deployments but not in architecture model -> should result in a warning will
 not be created and not simulated
 - if service is not specified in k8s deployments but in the architecture model -> automatically create deployment,
 autoscaler, load balancer, scheduler etc. from default values or entry from architecture file

A deployment (for the microservices ___users___) looks like this:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: users-deployment
  labels:
    app: users
spec:
  replicas: 3
  selector:
    matchLabels:
      app: users
  template:
    metadata:
      labels:
        app: users
    spec:
      schedulerName: kube
      containers:
        - name: users
          image: nginx:1.14.2
```
Important values are:
- ___kind___: Defines the kind of the k8 object (here deployment)
- ___metadata/name___: Name of the deployment
- ___replicas___: Amount of pods that should be available
- ___schedulerName___: (**Optional**) Name of the scheduler
- ___containers/name___: Name of the microservice given in the architecture file. The names must be equal to 
match the microservice to the deployment's container

Furthermore, it is possible to specify a HPA for any deployment:

```yaml
apiVersion: autoscaling/v2beta2
kind: HorizontalPodAutoscaler
metadata:
  name: users-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: users-deployment
  minReplicas: 1
  maxReplicas: 10
  metrics:
    - type: Resource
      resource:
        name: cpu
        target:
          type: Utilization
          averageUtilization: 50
```
Important values are:
- ___kind___: Defines the kind of the k8 object (here HorizontalPodAutoscaler)
- ___metadata/name___: Name of the HPA
- ___scaleTargetRef/kind___: Kind of the object that should be managed by this HPA (always Deployment)
- ___scaleTargetRef/name___: Name of the deployment that should be managed by this HPA
- ___minReplicas___: Minimum amount of pods that should be available
- ___maxReplicas___: Maximum amount of pods that should be available
- ___metrics___: This whole block needs to be added like this. Until now only cpu utilization is supported
- ___metrics/averageUtilization___: Amount of average utilization that should be valid for all pods of this deployment 
in percent


### Kube-Scheduler
It is possible to use the real kube-scheduler as scheduler for the simulation.
For that purpose... 
- start the api with the command ```uvicorn main:app```
- start the scheduler with the command ```./kube-scheduler --master 127.0.0.1:8000 --config config.txt```
- Run MiSim (with at least one deployment with the scheduler ___kube___)

IMPORTANT
<details>
  <summary>Make sure that the scheduler has the config.txt.</summary>

```
apiVersion: kubescheduler.config.k8s.io/v1beta3
clientConnection:
  acceptContentTypes: ""
  burst: 100
  contentType: application/vnd.kubernetes.protobuf
  kubeconfig: ""
  qps: 50
enableContentionProfiling: true
enableProfiling: true
kind: KubeSchedulerConfiguration
leaderElection:
  leaderElect: true
  leaseDuration: 15s
  renewDeadline: 10s
  resourceLock: leases
  resourceName: kube-scheduler
  resourceNamespace: kube-system
  retryPeriod: 2s
parallelism: 16
percentageOfNodesToScore: 0
podInitialBackoffSeconds: 1
podMaxBackoffSeconds: 1
profiles:
- pluginConfig:
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      kind: DefaultPreemptionArgs
      minCandidateNodesAbsolute: 100
      minCandidateNodesPercentage: 10
    name: DefaultPreemption
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      hardPodAffinityWeight: 1
      kind: InterPodAffinityArgs
    name: InterPodAffinity
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      kind: NodeAffinityArgs
    name: NodeAffinity
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      kind: NodeResourcesBalancedAllocationArgs
      resources:
      - name: cpu
        weight: 1
      - name: memory
        weight: 1
    name: NodeResourcesBalancedAllocation
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      kind: NodeResourcesFitArgs
      scoringStrategy:
        resources:
        - name: cpu
          weight: 1
        - name: memory
          weight: 1
        type: LeastAllocated
    name: NodeResourcesFit
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      defaultingType: System
      kind: PodTopologySpreadArgs
    name: PodTopologySpread
  - args:
      apiVersion: kubescheduler.config.k8s.io/v1beta3
      bindTimeoutSeconds: 600
      kind: VolumeBindingArgs
    name: VolumeBinding
  plugins:
    bind: {}
    filter: {}
    multiPoint:
      enabled:
      - name: PrioritySort
        weight: 0
      - name: NodeUnschedulable
        weight: 0
      - name: NodeName
        weight: 0
      - name: TaintToleration
        weight: 3
      - name: NodeAffinity
        weight: 2
      - name: NodePorts
        weight: 0
      - name: NodeResourcesFit
        weight: 1
      - name: VolumeRestrictions
        weight: 0
      - name: EBSLimits
        weight: 0
      - name: GCEPDLimits
        weight: 0
      - name: NodeVolumeLimits
        weight: 0
      - name: AzureDiskLimits
        weight: 0
      - name: VolumeBinding
        weight: 0
      - name: VolumeZone
        weight: 0
      - name: PodTopologySpread
        weight: 2
      - name: InterPodAffinity
        weight: 2
      - name: DefaultPreemption
        weight: 0
      - name: NodeResourcesBalancedAllocation
        weight: 1
      - name: ImageLocality
        weight: 1
      - name: DefaultBinder
        weight: 0
    permit: {}
    postBind: {}
    postFilter: {}
    preBind: {}
    preFilter: {}
    preScore: {}
    queueSort: {}
    reserve: {}
    score: {}
  schedulerName: my-scheduler
```

</details>


#### Node Affinity
It is possible to define node affinities (see https://kubernetes.io/docs/concepts/scheduling-eviction/assign-pod-node/)
Currently 
- exactly **one** requiredDuringSchedulingIgnoredDuringExecution, 
- **one** key ("personalized/name") and 
- **multiple** values (any String) are supported


```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: books-deployment
  labels:
    app: books
spec:
  replicas: 1
  selector:
    matchLabels:
      app: books
  template:
    metadata:
      labels:
        app: books
    spec:
      containers:
        - name: books
          image: booksApp:1.14.2
      affinity:
        nodeAffinity:
          requiredDuringSchedulingIgnoredDuringExecution:
            nodeSelectorTerms:
              - matchExpressions:
                  - key: personalized/name
                    operator: In
                    values:
                      - Large
```
