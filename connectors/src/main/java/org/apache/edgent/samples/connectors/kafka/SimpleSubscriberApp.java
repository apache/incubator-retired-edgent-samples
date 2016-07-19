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
package org.apache.edgent.samples.connectors.kafka;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.samples.connectors.Util;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import org.apache.edgent.connectors.kafka.KafkaConsumer;

/**
 * A simple Kafka subscriber topology application.
 */
public class SimpleSubscriberApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to kafka.properties file");
        SimpleSubscriberApp subscriber = new SimpleSubscriberApp(args[0]);
        subscriber.run();
    }

    /**
     * @param kafkaPropsPath pathname to properties file
     */
    SimpleSubscriberApp(String kafkaPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(kafkaPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private Map<String,Object> createKafkaConfig() {
        Map<String,Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put("zookeeper.connect", props.get("zookeeper.connect"));
        // for the sample, be insensitive to old/multiple consumers for
        // the topic/groupId hanging around
        kafkaConfig.put("group.id", 
                "kafkaSampleConsumer_" + Util.simpleTS().replaceAll(":", ""));
        return kafkaConfig;
    }
    
    /**
     * Create a topology for the subscriber application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("kafkaSampleSubscriber");
        
        // Create the Kafka Consumer broker connector
        Map<String,Object> kafkaConfig = createKafkaConfig();
        KafkaConsumer kafka = new KafkaConsumer(t, () -> kafkaConfig);
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = kafka.subscribe(rec -> rec.value(), topic);
        
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", Util.simpleTS(), tuple)));
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
