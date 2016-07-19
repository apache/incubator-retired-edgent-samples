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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.samples.connectors.Util;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import org.apache.edgent.connectors.kafka.KafkaProducer;

/**
 * A simple Kafka publisher topology application.
 */
public class SimplePublisherApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to kafka.properties file");
        SimplePublisherApp publisher = new SimplePublisherApp(args[0]);
        publisher.run();
    }

    /**
     * @param kafkaPropsPath pathname to properties file
     */
    SimplePublisherApp(String kafkaPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(kafkaPropsPath).toPath()));
        topic = props.getProperty("topic");
    }
    
    private Map<String,Object> createKafkaConfig() {
        Map<String,Object> kafkaConfig = new HashMap<>();
        kafkaConfig.put("bootstrap.servers", props.get("bootstrap.servers"));
        return kafkaConfig;
    }
    
    /**
     * Create a topology for the publisher application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("kafkaSamplePublisher");

        // Create the Kafka Producer broker connector
        Map<String,Object> kafkaConfig = createKafkaConfig();
        KafkaProducer kafka = new KafkaProducer(t, () -> kafkaConfig);
        
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
        kafka.publish(msgs, topic);
        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
