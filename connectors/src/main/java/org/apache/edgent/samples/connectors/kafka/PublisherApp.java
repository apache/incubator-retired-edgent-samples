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

import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_BOOTSTRAP_SERVERS;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_PUB_CNT;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_TOPIC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.samples.connectors.MsgSupplier;
import org.apache.edgent.samples.connectors.Options;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;
import org.apache.edgent.topology.TopologyProvider;

import org.apache.edgent.connectors.kafka.KafkaProducer;

/**
 * A Kafka producer/publisher topology application.
 */
public class PublisherApp {
    private final TopologyProvider tp;
    private final Options options;

    /**
     * @param tp the TopologyProvider to use.
     * @param options
     */
    PublisherApp(TopologyProvider tp, Options options) {
        this.tp = tp;
        this.options = options;
    }
    
    /**
     * Create a topology for the publisher application.
     * @return the Topology
     */
    public Topology buildAppTopology() {
        Topology t = tp.newTopology("kafkaClientPublisher");
        
        // Create a sample stream of tuples to publish
        TStream<String> msgs = t.poll(new MsgSupplier(options.get(OPT_PUB_CNT)),
                                        1L, TimeUnit.SECONDS);

        // Create the KafkaProducer broker connector
        Map<String,Object> config = newConfig();
        KafkaProducer kafka = new KafkaProducer(t, () -> config);
        
        // Publish the stream to the topic.  The String tuple is the message value.
        kafka.publish(msgs, options.get(OPT_TOPIC));
        
        return t;
    }
    
    private Map<String,Object> newConfig() {
        Map<String,Object> config = new HashMap<>();
        // required kafka configuration items
        config.put("bootstrap.servers", options.get(OPT_BOOTSTRAP_SERVERS));
        return config;
    }

}
