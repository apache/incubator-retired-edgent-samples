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
package org.apache.edgent.samples.apps.mqtt;

import org.apache.edgent.connectors.iot.QoS;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;
import org.apache.edgent.topology.json.JsonFunctions;

import com.google.gson.JsonObject;

/**
 * An MQTT Device Communications client for watching device events
 * and sending commands.
 * <p>
 * This is an "application properties" aware client that gets MQTT configuration
 * from an Edgent sample app application configuration properties file.
 * <p>
 * This client avoids the need for other MQTT clients (e.g., from a mosquitto
 * installation) to observe and control the applications.
 */
public class DeviceCommsApp extends AbstractMqttApplication {
    
    private static final String usage = "Usage: watch | send <cmdLabel> <cmdArg>";

    private String mode;
    private String cmdLabel;
    private String cmdArg;
    
    public static void main(String[] args) throws Exception {
        if (args.length < 1)
            throw new Exception("missing pathname to application properties file");
        
        try {
            int i = 0;
            DeviceCommsApp application = new DeviceCommsApp(args[i++]);
            String mode = args[i++];
            if (!("watch".equals(mode) || "send".equals(mode))) {
                throw new IllegalArgumentException("Unsupport mode: "+application.mode);
            }
            application.mode = mode;
            if (application.mode.equals("send")) {
                application.cmdLabel = args[i++];
                application.cmdArg = args[i++];
            }
        
            application.run();
        }
        catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e.getMessage()
                    +"\n"+usage);
        }
    }
    
    /**
     * Create an application instance.
     * @param propsPath pathname to an application configuration file
     * @throws Exception
     */
    DeviceCommsApp(String propsPath) throws Exception {
        super(propsPath);
    }
    
    @Override
    protected void buildTopology(Topology t) {
        mqttDevice().getMqttConfig().setClientId(null);
        MqttStreams mqtt = new MqttStreams(t, () -> mqttDevice().getMqttConfig());
        if (mode.equals("send")) {
            String topic = mqttDevice().commandTopic(cmdLabel);
            JsonObject jo = new JsonObject();
            jo.addProperty("value", cmdArg);
            System.out.println("Publishing command: topic="+topic+"  value="+jo);
            TStream<String> cmd = t.strings(JsonFunctions.asString().apply(jo));
            mqtt.publish(cmd, topic, QoS.FIRE_AND_FORGET, false/*retain*/);
            // Hmm... the paho MQTT *non-daemon* threads prevent the app
            // from exiting after returning from main() following job submit().
            // Lacking MqttStreams.shutdown() or such...
            // Delay a bit and then explicitly exit().  Ugh.
            cmd.sink(tuple -> { 
                try {
                    Thread.sleep(3*1000);
                } catch (Exception e) { }
                System.exit(0); });
        }
        else if (mode.equals("watch")) {
            String topicFilter = mqttDevice().eventTopic(null);
            System.out.println("Watching topic filter "+topicFilter);
            TStream<String> events = mqtt.subscribe(topicFilter, QoS.FIRE_AND_FORGET,
                    (topic,payload) -> { 
                        String s = "\n# topic "+topic;
                        s += "\n" + new String(payload);
                        return s;
                    });
            events.print();
        }
    }
}
