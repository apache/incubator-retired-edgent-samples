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
# Copyright IBM Corp. 2015, 2016 
*/
package quarks.samples.connectors.mqtt;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import quarks.connectors.mqtt.MqttConfig;
import quarks.connectors.mqtt.MqttStreams;
import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.samples.connectors.Util;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * A simple MQTT publisher topology application.
 */
public class SimplePublisherApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to mqtt.properties file");
        SimplePublisherApp publisher = new SimplePublisherApp(args[0]);
        publisher.run();
    }

    /**
     * @param mqttPropsPath pathname to properties file
     */
    SimplePublisherApp(String mqttPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(mqttPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private MqttConfig createMqttConfig() {
        MqttConfig mqttConfig = new MqttConfig(props.getProperty("serverURI"),
                                                null);
        // used if broker requires username/pw authentication
        mqttConfig.setUserName(props.getProperty("userID",
                                System.getProperty("user.name")));
        mqttConfig.setPassword(props.getProperty("password",
                                "myMosquittoPw").toCharArray());
        return mqttConfig;
    }
    
    /**
     * Create a topology for the publisher application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("mqttSamplePublisher");

        // Create the MQTT broker connector
        MqttConfig mqttConfig = createMqttConfig();
        MqttStreams mqtt = new MqttStreams(t, () -> mqttConfig);
        
        // Create a sample stream of tuples to publish
        AtomicInteger cnt = new AtomicInteger();
        TStream<String> msgs = t.poll(
                () -> {
                    String msg = String.format("Message-%d from %s",
                            cnt.incrementAndGet(), Util.simpleTS());
                    System.out.println("poll generated msg to publish: " + msg);
                    return msg;
                }, 1L, TimeUnit.SECONDS);
        
        // Publish the stream to the topic.  The String tuple is the message value.
        mqtt.publish(msgs, topic, 0/*qos*/, false/*retain*/);
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
