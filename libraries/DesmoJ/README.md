
## DesmoJ

To install Desmoj into the local maven repository by running the following command. Or execute one of the install-scripts. 

```batch
mvn install:install-file -Dfile=.\desmoj-2.5.1e-bin.jar -Dversion=2.5.1e -DgeneratePom=true -DgroupId=desmoj -DartifactId=desmoj -Dpackaging=jar
```

Change versions if necessary. Keep in mind to also change your pom accordingly.