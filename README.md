# REngine
REngine is a general Java interface to [R](https://www.R-project.org) supporting multiple back-ends such as [JRI](https://RForge.net/rJava) and [Rserve](https://RForge.net/Rserve). 

It allows Java code to evaluate R expressions, push and pull data. Since it is backend-agnostic, the same code can be used to embed R into a Java process or use a cluster of R servers to perform statisical computations.

The sources can be compiled simply by using

    make
   
Maven is also supported (including unit tests) and can be run using `make mvn.pkg`. If you want to run `mvn` by hamd you can do so, but note that the Maven project requires a different directory structure which can be created by calling `mkmvn.sh` (unix only as it uses symlinks).

[![REngine Actions Status](https://github.com/s-u/REngine/workflows/REngine/badge.svg)](https://github.com/s-u/REngine/actions)
