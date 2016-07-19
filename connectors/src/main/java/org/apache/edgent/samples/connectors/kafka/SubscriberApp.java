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

import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_GROUP_ID;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_TOPIC;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_ZOOKEEPER_CONNECT;

import java.util.HashMap;
import java.util.Map;

import org.apache.edgent.samples.connectors.Options;
import org.apache.edgent.samples.connectors.Util;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;
import org.apache.edgent.topology.TopologyProvider;

import org.apache.edgent.connectors.kafka.KafkaConsumer;

/**
 * A Kafka consumer/subscriber topology application.
 */
public class SubscriberApp {
    private final TopologyProvider tp;
    private final Options options;
    private final String uniq = Util.simpleTS();

    /**
     * @param top the TopologyProvider to use.
     * @param options
     */
    SubscriberApp(TopologyProvider tp, Options options) {
        this.tp = tp;
        this.options = options;
    }
    
    /**
     * Create a topology for the subscriber application.
     * @return the Topology
     */
    public Topology buildAppTopology() {
        Topology t = tp.newTopology("kafkaClientSubscriber");

        // Create the KafkaConsumer broker connector
        Map<String,Object> config = newConfig(t);
        KafkaConsumer kafka = new KafkaConsumer(t, () -> config);
        
        System.out.println("Using Kafka consumer group.id "
                            + config.get(OPT_GROUP_ID));
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = kafka.subscribe(rec -> rec.value(),
                                                (String)options.get(OPT_TOPIC));
        
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("[%s] received: %s", Util.simpleTS(), tuple)));
        
        return t;
    }
    
    private Map<String,Object> newConfig(Topology t) {
        Map<String,Object> config = new HashMap<>();
        // required kafka configuration items
        config.put("zookeeper.connect", options.get(OPT_ZOOKEEPER_CONNECT));
        config.put("group.id", options.get(OPT_GROUP_ID, newGroupId(t.getName())));
        return config;
    }
    
    private String newGroupId(String name) {
        // be insensitive to old consumers for the topic/groupId hanging around
        String groupId = name + "_" + uniq.replaceAll(":", "");
        return groupId;
    }
}
