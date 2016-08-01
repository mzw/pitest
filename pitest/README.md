# AdaMu-Extended Version
AdaMu is an implementation of adaptive mutation testing on top of PIT mutation testing tool.

## Setup

### Install the 3rd party library for AdaMu
```
mvn install:install-file \
	-Dfile=/path/to/pitest/pitest/lib/beast.jar \
	-DgroupId=dr.app \
	-DartifactId=beast \
	-Dversion=1.0 \
	-Dpackaging=jar \
	-DgeneratePom=true
	
mvn install:install-file \
    -Dfile=/path/to/pitest/pitest/lib/javastat_beta1.4.jar \
    -DgroupId=javastat \
    -DartifactId=javastat \
    -Dversion=1.4.beta \
    -Dpackaging=jar \
    -DgeneratePom=true
```
