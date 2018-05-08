# Simulation-based Resilience Prediction of Microservice Architectures

This simulator was created as part of the Fachstudie __Simulation-based Resilience Prediction of Microservice Architectures__ at the Reliable Software Systems Research Group of the Institute of Software Technology at the University of Stuttgart.

It allows the simulation of microservice architectures in regard to resilience and is based on the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation. 

**Table of contents:**
- [Installation](#Installation)
- [Input Model](#Input)
- [Usage](#Sim-Use)
- [Documentation](#Sim-Doc)

## <a name="Installation"></a>Installation

In order to run the simulator you have to download the DesmoJ binary from [sourceforge](http://desmoj.sourceforge.net/download.html) and then include it into the project.

## <a name="Input"></a>Input Model
The input for the simulator is a _json_ file which contains all the information necessary to simulate the architecture.
The following is a simple example for the _json_ input:


```json
{
  "simulation": {
    "experiment": "example",
    "model": "mModel",
    "duration": "1000",
    "report": "",
    "datapoints": "1000",
    "seed": ""
  },
  "microservices": [
    {
      "name": "frontend",
      "instances": "3",
      "patterns": [
      	{
      		"name": "Resource Limiter",
		"arguments": [
			1,
			2
		]
      	}
      ],
      "capacity": "2000",
      "operations": [
        {
          "name": "login",
          "demand": "150",
          "patterns": [
            "Circuit Breaker"
          ],
          "dependencies": [
            {
              "service": "authentication",
              "operation": "authenticate",
              "probability": "0.9"
            }
          ]
        }
      ]
    },
    {
      "name": "authentication",
      "instances": "2",
      "capacity": "2000",
      "operations": [
        {
          "name": "authenticate",
          "demand": "100",
          "patterns": [
            "Circuit Breaker"
          ]
        }
      ]
    }
  ],
  "generators": [
    {
      "service": "frontend",
      "operation": "login",
      "interval": "10"
    }
  ],
  "chaosmonkeys": [
    {
      "service": "frontend",
      "instances": "1",
      "interval": "50"
    }
  ]
}
```

### Description
The input basically consits of four main components. These are:
- Simulation
- Microservices
- Generators
- Chaosmonkeys

#### Simulation
The _simulation_ object holds general information about the simulation experiment. 

- ___experiment___: Name of the experiment
- ___model___: Name of the employed model
- ___duration___: Desired duration of the simulation in seconds
- ___report___: The simulator creates a report at the end of the simulation. Leave this field __empty__ if you want a detailed report, set the value to _"minimalistic"_ if you want a minimalistic version of the report or set it to _"none"_ if you don't want a report
- ___datapoints___: The number of datapoints you want for the charts in your report. The simulator records statistics at every datapoint. If you set the value to _"0"_ no charts will be created. If you set it to _"-1"_ the simulator will record a datapoint at every simulated second
- ___seed___: A seed for the randomly generated events in the simulator. Leave this field __empty__ if you want random experiments or set the value to an integer to use a seed

#### Microservices
The _microservices_ array holds objects which contain information about respectively one microservice and their dependcies. Together they compose the architecture that is simulated.

- ___name___: Name of the microservice
- ___instances___: Number of instances of this microservice
- ___capacity___: CPU capacity of _each_ instance in Mhz
- ___patterns___: Array of resilience patterns that are implemented in this microservice. The array contains objects which hold information about the respective resilience pattern
	- ___name___: The name of the pattern. As of now the only supported pattern is _Resource Limiter_
	- ___arguments___: An array which contains parameters about the pattern
- ___operations___: Array which holds objects which contain information about the different operations that this microservice can perform
	- ___name___: Name of the operation
	- ___demand___: CPU demand of this operation in Mhz
	- ___patterns___: Array of resilience patterns which are implemented in the specific operation. For now the simulator can simulate the _Circuit Breaker_ pattern
	- ___dependencies___: Arry of objects which hold information about a dependency that this operation has with another operation.
		- ___service___: Name of the microservice to which the other operation belongs
		- ___operation___: Name of the other operation from which this operation depends
		- ___probability___: The probability that this operation will call the other operation (decimal number in between 0 and 1)
	
#### Generators
The _generators_ array holds objects which contain information about the generation of inital requests to different microservices of the system to start the simulation.

- ___service___: Name of the microservice to which the request should be send
- ___operation___: Name of the operation which should be performed
- ___time___: Time interval in seconds in which these requests will be created	

#### Chaosmonkeys
The _chaosmonkeys_ array holds objects which contain information about chaos monkeys which shut down instances of specified microservices during the simulation.

- ___service___: Name of the service of which you want to shut down a number of instances during the simulation
- ___instances___: Number of instances you want to shut down during the simulation
- ___time___: Time point (in seconds) at which you want to shut down the instances of the specified microservie
