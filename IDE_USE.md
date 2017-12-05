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

A maven-integrated IDE such as Eclipse or IntelliJ can easily be used
to build the samples and the application template project.

If your IDE lacks a maven integration see `samples/get-edgent-jars-project`
for a tool to create a bundle containing the Edgent runtime jars. You can
unpack the bundle and adjust the projects in your IDE to use them.


# Using Eclipse

The Edgent Git repository and samples source release bundle contains 
Maven project definitions for the samples.

Once you import the Maven projects into your workspace, builds
in Eclipse use the same artifacts as the Maven command line tooling. 
Like the command line tooling, the jars for dependent projects
are automatically downloaded to the local maven repository
and used.

Note: Specifics may change depending on your version of Eclipse or the 
Eclipse Maven or Git Team Provider.

# Download the samples

1. Clone or download the samples as described in [README.md](README.md).

# Import the samples

1. From the Eclipse *File* menu, select *Import...*
2. From the *Maven* folder, select *Existing Maven Projects* and click *Next*
  + browse to the `samples` directory in the clone or source release directory
    and select it.  A hierarchy of samples projects / pom.xml files will be
    listed and all selected. 
  + Verify the *Add project(s) to working set* checkbox is checked
  + Click *Finish*.  Eclipse starts the import process and builds the workspace.

Top-level artifacts such as `README.md` are available under the 
`edgent-samples` project.

# Run a sample

Once the samples projects have been imported you can run them from
Eclipse in the usual manner. E.g.,

1. From the Eclipse *Navigate* menu, select *Open Type*
   + enter type name `HelloEdgent` and click *OK*
2. right click on the `HelloEdgent` class name and from the context menu
   + click on *Run As*, then *Java application*.
   `HelloEdgent` runs and prints to the Console view.

# Cloning the application template

See [template/README.md](template/README.md) for general information.

To clone the template project for your application project:

1. Recursively copy the template folder into a new folder from the command line, e.g.,
   from the unpacked samples folder
   + cp -R template ~/myApp
2. Import the new project into your Eclipse workspace
   1. from the Eclipse *File* menu, select *Import...*
   2. from the *Maven* folder, select *Existing Maven Projects* and click *Next*
      + browse to the new folder and select it.  The project's pom.xml file will be
        listed and selected. 
      + click *Finish*.  Eclipse starts the import process and builds the workspace.
        Note, the new imported project's name will be `my-app`.
        This can be renamed later.

Verify you can run the imported template app:

1. From the Eclipse *Navigate* menu, select *Open Type*
   + enter type name `TemplateApp` and click *OK*
2. right click on the `TemplateApp` class name and from the context menu
   + click on *Run As*, then *Java application*.  
  `TemplateApp` runs and prints to the Console view.

You can then start adding your application's java code
and adjusting the template's pom for your application's
dependencies.
