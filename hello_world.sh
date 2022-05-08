#!/bin/sh

mvn -B clean package  --file pom.xml "-DskipTests=true" "-Dmaven.javadoc.skip=true" "-Dcheckstyle.skipExec=true"


java -jar ./target/misim.jar -a ./Examples/example_architecture_model.json -e ./Examples/example_experiment_chaosmonkey.json -o ./HelloWorldResults

exit $?