<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

Getting started with the Edgent samples is a great way to start using
Edgent and jump-start your Edgent application development.

See [IDE Quickstart](IDE_USE.md) if you want use an IDE instead
of the command line.

# Quickstart

Convenience binaries (jars) for the Edgent runtime releases are distributed
to the ASF Nexus Repository and the Maven Central Repository.  You don't have
to manually download the Edgent jars and there is no need to download the 
Edgent runtime sources and build them unless you want to.

By default the samples depend on Java8.  Download and install Java8 if needed.

## Download the Edgent Samples

Get the Edgent Samples either by cloning or downloading the [Edgent Samples GitHub repository](https://github.com/apache/incubator-edgent-samples):
```sh
git clone https://github.com/apache/incubator-edgent-samples
cd incubator-edgent-samples
git checkout develop
```
or to download:
  + Open the _Clone or download_ pulldown at the [Edgent Samples GitHub repository](https://github.com/apache/incubator-edgent-samples)
  + Click on _Download ZIP_
  + Unpack the downloaded ZIP
```sh
    unzip incubator-edgent-samples-develop.zip
```

## Build the Samples
```sh
cd <the cloned or unpacked samples folder>
./mvnw clean package  # build for Java8
```

## Run the HelloEdgent sample
```sh
cd topology
./run-sample.sh HelloEdgent   # prints a hello message and terminates
  Hello
  Edgent!
  ...
```

# Overview

The Edgent samples are organized into a few categories (subdirectories)
and are a collection of maven projects.  They can be built using maven
or other maven-integrated tooling such as Eclipse - see
[IDE Quickstart](IDE_USE.md).

See the _Samples Summary_ section for a list of the samples.

An Edgent application template maven project is supplied.
It may be a useful starting point to clone for your application.
The template has a standalone maven project pom rather than the 
stylized one used by the rest of the samples. 
See [template/README.md](template/README.md).

See [APPLICATION_DEVELOPMENT.md](APPLICATION_DEVELOPMENT.md) for general
information on Edgent Application Development, Packaging and Execution.

Additional information may also be found in
Getting Started https://edgent.apache.org/docs/edgent-getting-started


# Setup

Once you have downloaded and unpacked the samples source bundle 
or cloned the the samples repository you need to download
these additional development software tools.

* Java 8 - The development setup assumes Java 8

Maven is used as build tool and a maven-wrapper
script (`mvwn` or `mvnw.bat`) is included.

The maven-wrapper automatically downloads and installs the
correct Maven version and uses it. Besides this, there is no
difference between using the `mvnw` command and the `mvn` command. 

The samples use Edgent SDK jars that have been released
in a maven repository such as Maven Central.

Alternatively, you can download the Edgent SDK sources and build them.
See [downloads](https://edgent.apache.org/docs/downloads) 
for downloading the Edgent SDK sources.

# Building the Edgent samples

By default Java8 class files are generated.
Java7 platform class files are produced when the appropriate
profile is specified.

Currently, building and running the samples for the Android platform
is not supported.  Many samples happen to use the `DevelopmentProvider`,
which is not supported on the Android platform.

Build the samples
```sh
./mvnw clean package  # -Pplatform-java7 as needed
```

A standard jar and uber jar are created for each sample category
in the sample category's target directory: `<category>/target`.


## Building against a different Edgent runtime version

To change the version of the Edgent runtime artifacts used,
edit the `edgent.runtime.version` property in the top level
`pom.xml`. e.g.,
```xml
    <edgent.runtime.version>1.3.0-SNAPSHOT</edgent.runtime.version>
```

Note: Do NOT override the value via
`./mvnw ... -Dedgent.runtime.version=<the-version>`.
The build will not behave as desired.

A similar declaration is present in template/pom.xml.
When running get-edgent-jars.sh, specify `--version=<the-version>`.


# Running the samples

See the `README.md` in each sample category directory for information
on running the samples.

# Samples Summary

<pre>
HelloEdgent          Basic mechanics of declaring a topology and executing
                     it. Prints Hello Edgent! to standard output.
        
TempSensorApplication A basic Edgent application used by the Edgent
                     "Getting Started Guide":
                     https://edgent.apache.org/docs/edgent-getting-started.html
                     
PeriodicSource       Create a stream by polling a random number generator
                     for a new value every second and then prints out the
                     raw tuple value and a filtered and transformed stream.
                          
SensorAggregates     Demonstrates partitioned window aggregation and 
                     filtering of simulated sensors that are bursty in
                     nature, so that only intermittently is the data output
                     to standard output.
                         
File                 Use the File stream connector to write a stream of
                     tuples to files. Also watch a directory for new files
                     and create a stream of tuples from the file contents.
                         
Iotp                 Use the IBM Watson IoT Platform connector to send
                     simulated sensor readings to an IBM Watson IoT Platform
                     instance as device events. Receive device commands.
                         
JDBC                 Use the JDBC stream connector to write a stream of
                     tuples to an Apache Derby database table. Create a
                     stream of tuples by reading a table.
                         
Kafka                Use the Kafka stream connector to publish a stream of
                     tuples to a Kafka topic. Create a stream of tuples by
                     subscribing to a topic and receiving messages from it.
                         
MQTT                 Use the MQTT stream connector to publish a stream of
                     tuples to a MQTT topic. Create a stream of tuples by
                     subscribing to a topic and receiving messages from it.
                         
SensorAnalytics      Demonstrates a more complex sample that includes 
                     configuration control, a device of one or more sensors
                     and some typical analytics, use of MQTT for publishing
                     results and receiving commands, local results logging,
                     conditional stream tracing.
</pre>

Many other samples are provided but have not yet been noted above. Explore!

# Licensing

Apache Edgent samples are released under the Apache License Version 2.0.

Apache Edgent is an effort undergoing incubation at The Apache Software Foundation (ASF),
sponsored by the Incubator PMC. Incubation is required of all newly accepted
projects until a further review indicates that the infrastructure, communications,
and decision making process have stabilized in a manner consistent with other
successful ASF projects. While incubation status is not necessarily a reflection
of the completeness or stability of the code, it does indicate that the project
has yet to be fully endorsed by the ASF.
