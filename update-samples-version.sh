#!/bin/bash

################################################################################
##
##  Licensed to the Apache Software Foundation (ASF) under one or more
##  contributor license agreements.  See the NOTICE file distributed with
##  this work for additional information regarding copyright ownership.
##  The ASF licenses this file to You under the Apache License, Version 2.0
##  (the "License"); you may not use this file except in compliance with
##  the License.  You may obtain a copy of the License at
##
##      http://www.apache.org/licenses/LICENSE-2.0
##
##  Unless required by applicable law or agreed to in writing, software
##  distributed under the License is distributed on an "AS IS" BASIS,
##  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
##  See the License for the specific language governing permissions and
##  limitations under the License.
##
################################################################################

set -e

# Change the version of the edgent runtime that the tooling uses
# and/or change the version of the samples artifacts
#
# Generally, by default build against the (latest) edgent runtime version
# (e.g., "1.2.0").
# Generally, if/when it's decided to update the sample artifact versions
# they should remain a -SNAPSHOT version.

USAGE="usage: `basename $0` [--edgent-version <version>] [--samples-version <version>]"

EDGENT_VER=
if [ "${1}" = "--edgent-version" -a $# -gt 1 ] ; then
  EDGENT_VER=$2; shift; shift
fi

SAMPLES_VER=
if [ "${1}" = "--samples-version" -a $# -gt 1 ] ; then
  SAMPLES_VER=$2; shift; shift
fi

if [ "${EDGENT_VER}" = "" -a "${SAMPLES_VER}" = "" ] ; then
  echo ${USAGE}
  exit 1;
fi

if [ $# -gt 0 ] ; then
  echo ${USAGE}
  exit 1;
fi

# yeah, this should be some xslt processing

# update the edgent.runtime.version property
if [ "${EDGENT_VER}" != "" ] ; then
    echo updating the edgent.runtime.version property ...
    POMS=`find . -name pom.xml | grep -v target`
    POMS="${POMS} `find . -name pom.xml.template | grep -v target`"
    for POM in ${POMS}; do
      if [ `grep -s '<edgent.runtime.version>' ${POM}` ] ; then
        echo updating ${POM} for edgent.runtime.version ... 
        sed -i -e "s,<edgent.runtime.version>.*</edgent.runtime.version>,<edgent.runtime.version>${EDGENT_VER}</edgent.runtime.version>," ${POM}
        mv ${POM}-e ${POM}~
      fi
    done 
fi

# update the sample artifact ids
if [ "${SAMPLES_VER}" != "" ] ; then
    echo updating the sample artifact versions ...

    # update get-edgent-jars/pom.xml.template's artifact version
    POM=./get-edgent-jars-project/pom.xml.template
    echo updating ${POM} ...
    cp ${POM} ${POM}~ 
    awk "!done && /<version>/ { print \"  <version>${SAMPLES_VER}</version>\"; done=1; next;}; \
         1;" < ${POM}~ >${POM}

    # update the other poms
    # skip template/pom.xml as it's artifact version never changes
    POMS=`find . -name pom.xml | grep -v template | grep -v target`
    for POM in ${POMS} ; do
      echo updating ${POM} ...
      cp ${POM} ${POM}~
      if [ "${POM}" = "./pom.xml" ] ; then
        # change the artifact's <version> (after the <parent> spec)
        awk "\
           !firstMatch && /<\/parent>/ { firstMatch=1; print \$0; next; }; \
           firstMatch && !done && /<version>/ { print \"  <version>${SAMPLES_VER}</version>\"; done=1; next;}; \
           1;" < ${POM}~ >${POM}
      else
        # change the <parent> spec's <version> (the child sample pom's artifact implicitly inherits it)
        awk "\
           !firstMatch && /<parent>/ { firstMatch=1; print \$0; next; }; \
           firstMatch && !done && /<version>/ { print \"    <version>${SAMPLES_VER}</version>\"; done=1; next;}; \
           1;" < ${POM}~ >${POM}
      fi
    done
fi