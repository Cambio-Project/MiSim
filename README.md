[![Build and Test](https://github.com/Cambio-Project/MiSim/actions/workflows/build_artifact.yml/badge.svg)](https://github.com/Cambio-Project/MiSim/actions/workflows/build_artifact.yml)
[![Documentation](https://img.shields.io/badge/Documentation-online-32CA55?style=flat&logo=github&logoColor=959DA5&labelColor=2F353C)](https://Cambio-Project.github.io/MiSim/)<br>
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.6783250.svg)](https://doi.org/10.5281/zenodo.6783250) <br>

# MiSim - Microservice Resilience Simulator

MiSim allows the simulation of microservice architectures regarding resilience and is based on the [DesmoJ](http://desmoj.sourceforge.net) framework for discrete event modelling and simulation. 

See the [Wiki](https://github.com/Cambio-Project/MiSim/wiki) for further reference and usage information.


## Artifact Download

Download the [newest stable release](https://github.com/Cambio-Project/MiSim/releases) or a [nightly build](https://github.com/Cambio-Project/MiSim/actions/workflows/build_artifact.yml) (see artifacts of newest workflow run).

## Build from source

Clone via git and run 
`mvn -B package --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"`. 
You should see a `misim.jar` file in the resulting `target/` directory.

## Execute

Note that MiSim should be executed using Java 18 (or previous). Newer Java versions are likely to result in failing or corrupted simulation runs!
