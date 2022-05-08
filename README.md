[![Build Artifact](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml/badge.svg)](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml)
[![Documentation](https://img.shields.io/badge/Documentation-online-32CA55?style=flat&logo=github&logoColor=959DA5&labelColor=2F353C)](https://Cambio-Project.github.io/resilience-simulator/)
[![DOI](https://zenodo.org/badge/200825938.svg)](https://zenodo.org/badge/latestdoi/200825938)

## WIP Note: This ReadMe is still WIP. Some information is not true for MiSim 3.0 onwards.

# MiSim - Microservice Resilience Simulator

This simulator was created as part of two Bachelor Thesis and a Fachstudie **Simulation-based Resilience Prediction of Microservice Architectures** at the Software Quality Group of the Institute of Software Engineering at the University of Stuttgart.

It allows the simulation of microservice architectures in regard to resilience and is based on the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation.

## Features

### Feature Overview

- Simulation of Resilience Features
  - Retry
  - Auto Scaling
  - Load Balancing
  - Circuit Breaker
  - Connection Pool (limits number of concurrent requests | part of Circuit Breaker)
- Simulation of Chaos Injections
  - Instance Killing
  - Network Delay
- Manual starting of Instances
- Multi-Core CPU-Simulation
- support for [LIMBO](https://se.informatik.uni-wuerzburg.de/software-engineering-group/tools/limbo/) load profiles
- easy architecture and experiment descriptions
- Support for experiments in scenario form (see [ATAM](https://doi.org/10.1109/ICECCS.1998.706657), [Resources about the ATAM](https://resources.sei.cmu.edu/library/asset-view.cfm?assetid=513908))
- headless
- lightweight (~11MB)
- non-interactive
- Supporting tools that can create architectures and (scenario-based) experiments
- easily extendable with new strategies

#### Planned Features:

- CPU Load Injection
- Network Load Injection
- Simulation of Caching
- Auto-Self Restarting
- Output of Error Rates
- Output of Message/Execution Traces

### Output Metrics

See [Metrics](./docs/metrics)

## <a name="Usage"></a>Usage

### Artifact Download

Download the [newest stable release](https://github.com/Cambio-Project/resilience-simulator/releases) or a [nightly build](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml) (see artifacts of newest workflow run).

### Build from source

Clone via git and run
`mvn -B package --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"`.
You should see a `misim.jar` file in the resulting `target/` directory.

### Execution

Simply run `java -jar misim.jar [arguments]`.

### Parameters

| Argument     | Short | Required | Description                            | Example                                      |
| ------------ | ----- | -------- | -------------------------------------- | -------------------------------------------- |
| --arch_model | -a    | true     | provides path to the architecture file | ./Examples/example_architecture_scaling.json |

## <a name="Execution"></a>Execution

The simulation works only when the relative path `./Report` exists in execution directory.
With the following file structure ...

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

The architectural model is required as input for the simulator. It is saved in a _JSON_ file.
The following is a simple example for the architectural model:

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

The model contains architectural information about the microservices of the system, their operations and dependencies and the resilience patterns they implement.

- **_name_**: Name of the microservice
- **_instances_**: Number of instances of this microservice
- **_capacity_**: CPU capacity of _each_ instance in Mhz
- **_patterns_**: Array of resilience patterns that are implemented in this microservice. The array contains objects which hold information about the respective resilience pattern
  - **_name_**: The name of the pattern. As of now the only supported pattern is _Resource Limiter_
  - **_arguments_**: An array which contains parameters about the pattern
- **_operations_**: Array which holds objects which contain information about the different operations that this microservice can perform
  - **_name_**: Name of the operation
  - **_demand_**: CPU demand of this operation in Mhz
  - **_circuitBreaker_**: Contains the following parameters that configure the implemented circuit breaker: _rollingWindow_, _requestVolumeThreshold_, _errorThresholdPercentage_, _sleepWindow_ and _timeout_. If the operations doesn't implement a circuit breaker the value is _null_
  - **_dependencies_**: Arry of objects which hold information about a dependency that this operation has with another operation.
    - **_service_**: Name of the microservice to which the other operation belongs
    - **_operation_**: Name of the other operation from which this operation depends
    - **_probability_**: The probability that this operation will call the other operation (decimal number in between 0 and 1)

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

- **_experiment_name_**: Name of the experiment
- **_model_name_**: Name of the used model
- **_duration_**: The duration of the experiment in seconds, must be an integer
- **_report_**: The simulator creates a report at the end of the simulation. Leave this field empty if you want a detailed report, set the value to "minimalistic" if you want a minimalistic version of the report or set it to "none" if you don't want a report
- **_datapoints_**: The number of datapoints you want for the charts in your report. The simulator records statistics at every datapoint. If you set the value to "0" no charts will be created. If you set it to "-1" the simulator will record a datapoint at every simulated second
- **_seed_**: A seed for the randomly generated events in the simulator. Leave this field empty if you want random experiments or set the value to an integer to use a seed

### Request Generators

The _request_generators_ array holds objects which contain information about the generation of inital requests to different microservices of the system to start the simulation.

- **_service_**: Name of the microservice to which the request should be send
- **_operation_**: Name of the operation which should be performed
- **_interval_**: Time interval in seconds in which these requests will be created

### Chaosmonkeys

The _chaosmonkeys_ array holds objects which contain information about chaos monkeys which shut down instances of specified microservices during the simulation.

- **_service_**: Name of the service of which you want to shut down a number of instances during the simulation
- **_instances_**: Number of instances you want to shut down during the simulation
- **_time_**: Time point (in seconds) at which you want to shut down the instances of the specified microservie
