This is an Edgent Application template project.

The project's pom supports

- building for java8, java7 or android execution environments
- building a standard jar and uber jar

# Quickstart

Clone this template project folder to start creating your
Edgent application project.

## Cloning the template

In the unpacked samples folder

```sh
cp -R template ~/myApp
```

Verify the setup

```sh
cd ~/myApp
./mvnw clean package
./app-run.sh  # prints a hello message
```

## Customize

Edit the pom for your application.  Adjust it for your application's maven coordinates.
The pom has potential Edgent dependenacies present and commented out.
Include the Edgent Providers, Analytics, Utils, and Connectors used by your application.

See `../README.md` for general information about Edgent Application development.

# Overview

The template includes a maven wrapper script to eliminate the need to
manually download and install maven.

# Building the project
```sh
./mvnw clean package  # add -Pplatform-java7 or -Pplatform-android as needed
```

## Building against a different Edgent runtime version

To change the version of the Edgent runtime artifacts used,
edit the `edgent.runtime.version` property in the template's
`pom.xml`. e.g.,
```xml
    <edgent.runtime.version>1.3.0-SNAPSHOT</edgent.runtime.version>
```

Note: Do NOT override the value via
`./mvnw ... -Dedgent.runtime.version=<the-version>`.
The build will not behave as desired.

# Running the application

You can copy `app-run.sh` and the generated `target/*-uber.jar` to the 
edge device and then run it
```sh
./app-run.sh
```

# Using package-app.sh with the project

Adjust the main class name and application jar path below for your application.
```sh
PLATFORM=  # add "--platform java7"  or  "--platform android" as appropriate
../package-app.sh $PLATFORM --mainClass com.mycompany.app.TemplateApp --appjar target/my-app-1.0-SNAPSHOT.jar
```

# Using get-edgent-jars-project

If you don't want to use the generated uber jar or `package-app.sh`
approaches, you can copy the application's standard jar and a
`get-edgent-jars-project` generated jar bundle to the edge device.
See `samples/get-edgent-jars-project`.
