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
package org.apache.edgent.samples.apps.sensorAnalytics;

import org.apache.edgent.samples.apps.mqtt.AbstractMqttApplication;
import org.apache.edgent.topology.Topology;

/**
 * A sample application demonstrating some common sensor analytic processing
 * themes.
 */
public class SensorAnalyticsApplication extends AbstractMqttApplication {
    
    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to application properties file");
        
        SensorAnalyticsApplication application = new SensorAnalyticsApplication(args[0]);
        
        application.run();
    }
    
    /**
     * Create an application instance.
     * @param propsPath pathname to an application configuration file
     * @throws Exception
     */
    SensorAnalyticsApplication(String propsPath) throws Exception {
        super(propsPath);
    }
    
    @Override
    protected void buildTopology(Topology t) {
        
        // Add the "sensor1" analytics to the topology
        new Sensor1(t, this).addAnalytics();
        
        // TODO Add the "sensor2" analytics to the topology
        // TODO Add the "sensor3" analytics to the topology
    }
}
