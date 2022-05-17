[![Build Artifact](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml/badge.svg)](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml)
[![Documentation](https://img.shields.io/badge/Documentation-online-32CA55?style=flat&logo=github&logoColor=959DA5&labelColor=2F353C)](https://Cambio-Project.github.io/resilience-simulator/)

# MiSim - Microservice Resilience Simulator

MiSim allows the simulation of microservice architectures in regard to resilience and is based on the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation. 

See the [Wiki](https://github.com/Cambio-Project/resilience-simulator/wiki) for further reference and usage information.


## Artifact Download

Download the [newest stable release](https://github.com/Cambio-Project/resilience-simulator/releases) or a [nightly build](https://github.com/Cambio-Project/resilience-simulator/actions/workflows/build_artifact.yml) (see artifacts of newest workflow run).

## Build from source

Clone via git and run 
`mvn -B package --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"`. 
You should see a `misim.jar` file in the resulting `target/` directory.
