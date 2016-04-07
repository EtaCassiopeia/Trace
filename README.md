# Trace

Trace is a distributed tracing system. It helps gather timing data needed to track application`s flow.A "waterfall" style graph of service calls would be used to show event occurring and timeline.

### compile and build :
use `sbt package copy-dependencies` command to compile and package the project artifacts.

### configureations :
change configurations from **trace.properties** file

### run span stream:
use below command to run the project :

```java
spark-submit  --master spark://localhost:7077 --name trace --properties-file  trace.properties 
   --jars $(echo lib/*.jar | tr ' ' ',') --class org.github.etacassiopeia.stream.SpanStream
   stream_2.11-0.1.0.jar
```
