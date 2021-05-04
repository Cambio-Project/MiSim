
## Desmo-J

Desmo-J is being used as the simulation engine for this Project. Unfortunately its only distributed via [this](http://desmoj.sourceforge.net/home.html) website.

To ease the useage of this project, we provide the Desmo-J jar directly.

### As Maven Project (recommended)

To make Desmo-J available for a local maven repository, run the following command in this folder:

```
mvn install:install-file -Dfile=.\desmoj-2.5.1e-bin.jar -Dversion=2.5.1e -DgeneratePom=true -DgroupId=desmoj -DartifactId=desmoj -Dpackaging=jar
```

Change versions if necessary. If you do so, keep in mind to also change your pom.xml accordingly.


### As Intelli-J Project

Navigate to `File/Project Structure/Project Settings/Libraries` hit the `Plus`-button and search for the Desmo-J jar on your PC and click `OK`.

Keep in mind that maven will not successfully compile afterwards unless you also add a
`<systemPath>${basedir}/libraries/DesmoJ/desmoj-2.5.1e-bin.jar</systemPath>` property to your desmoj-maven dependency. This may also work for <b>Eclipse</b> (untested).
