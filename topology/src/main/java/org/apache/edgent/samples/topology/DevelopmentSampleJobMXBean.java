/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.edgent.samples.topology;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

public class DevelopmentSampleJobMXBean {
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentSampleJobMXBean");
        
        Random r = new Random();
        
        TStream<Double> d  = t.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);
        
        d.sink(tuple -> System.out.print("."));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
        
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();

        StringBuffer sbuf = new StringBuffer();
        sbuf.append(DevelopmentProvider.JMX_DOMAIN);
        sbuf.append(":interface=");
        sbuf.append(ObjectName.quote("org.apache.edgent.execution.mbeans.JobMXBean"));
        sbuf.append(",type=");
        sbuf.append(ObjectName.quote("job"));
        sbuf.append(",*");
        
        System.out.println("Looking for MBeans of type job: " + sbuf.toString());
        
        ObjectName jobObjName = new ObjectName(sbuf.toString());
        Set<ObjectInstance> jobInstances = mBeanServer.queryMBeans(jobObjName, null);
        Iterator<ObjectInstance> jobIterator = jobInstances.iterator();

        while (jobIterator.hasNext()) {
        	ObjectInstance jobInstance = jobIterator.next();
        	ObjectName objectName = jobInstance.getObjectName();

        	String jobId = (String) mBeanServer.getAttribute(objectName, "Id");
        	String jobName = (String) mBeanServer.getAttribute(objectName, "Name");
        	String jobCurState = (String) mBeanServer.getAttribute(objectName, "CurrentState");
        	String jobNextState = (String) mBeanServer.getAttribute(objectName, "NextState");
            String jobHealth = (String) mBeanServer.getAttribute(objectName, "Health");
            String jobLastError = (String) mBeanServer.getAttribute(objectName, "LastError");
        	
        	System.out.println("Found a job with JobId: " + jobId + " Name: " + jobName + 
                    " CurrentState: " + jobCurState + " NextState: " + jobNextState + 
                    " Health: " + jobHealth + " LastError: \"" + jobLastError + "\"");
        }
    }
}
