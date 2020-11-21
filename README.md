# MiSim Microservice Resilience Simulator

This simulator was created as part of the Fachstudie __Simulation-based Resilience Prediction of Microservice Architectures__ at the Reliable Software Systems Research Group of the Institute of Software Technology at the University of Stuttgart.

It allows the simulation of microservice architectures in regard to resilience and is based on the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation. 

**Table of contents:**
- [Installation](#Installation)
- [Execution](#Execution)
- [Architectural Model](#arch_mod)
- [Experiment Model](#exp_mod)

## <a name="Installation"></a>Installation

In order to run the simulator you have to download the DesmoJ binary from [sourceforge](http://desmoj.sourceforge.net/download.html) and then include it into the project.

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

- ___name___: Name of the microservice
- ___instances___: Number of instances of this microservice
- ___capacity___: CPU capacity of _each_ instance in Mhz
- ___patterns___: Array of resilience patterns that are implemented in this microservice. The array contains objects which hold information about the respective resilience pattern
	- ___name___: The name of the pattern. As of now the only supported pattern is _Resource Limiter_
	- ___arguments___: An array which contains parameters about the pattern
- ___operations___: Array which holds objects which contain information about the different operations that this microservice can perform
	- ___name___: Name of the operation
	- ___demand___: CPU demand of this operation in Mhz
	- ___circuitBreaker___: Contains the following parameters that configure the implemented circuit breaker: *rollingWindow*, *requestVolumeThreshold*, *errorThresholdPercentage*, *sleepWindow* and *timeout*. If the operations doesn't implement a circuit breaker the value is *null*
	- ___dependencies___: Arry of objects which hold information about a dependency that this operation has with another operation.
		- ___service___: Name of the microservice to which the other operation belongs
		- ___operation___: Name of the other operation from which this operation depends
		- ___probability___: The probability that this operation will call the other operation (decimal number in between 0 and 1)

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
- ___report___: The simulator creates a report at the end of the simulation. Leave this field empty if you want a detailed report, set the value to "minimalistic" if you want a minimalistic version of the report or set it to "none" if you don't want a report
- ___datapoints___: The number of datapoints you want for the charts in your report. The simulator records statistics at every datapoint. If you set the value to "0" no charts will be created. If you set it to "-1" the simulator will record a datapoint at every simulated second
- ___seed___: A seed for the randomly generated events in the simulator. Leave this field empty if you want random experiments or set the value to an integer to use a seed

### Request Generators
The _request_generators_ array holds objects which contain information about the generation of inital requests to different microservices of the system to start the simulation.

- ___service___: Name of the microservice to which the request should be send
- ___operation___: Name of the operation which should be performed
- ___interval___: Time interval in seconds in which these requests will be created	

### Chaosmonkeys
The _chaosmonkeys_ array holds objects which contain information about chaos monkeys which shut down instances of specified microservices during the simulation.

- ___service___: Name of the service of which you want to shut down a number of instances during the simulation
- ___instances___: Number of instances you want to shut down during the simulation
- ___time___: Time point (in seconds) at which you want to shut down the instances of the specified microservie
