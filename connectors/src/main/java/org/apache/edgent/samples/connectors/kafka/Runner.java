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
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_PUB;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_TOPIC;
import static org.apache.edgent.samples.connectors.kafka.KafkaClient.OPT_ZOOKEEPER_CONNECT;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.samples.connectors.Options;
import org.apache.edgent.topology.Topology;

/**
 * Build and run the publisher or subscriber application.
 */
public class Runner {
    /**
     * Build and run the publisher or subscriber application.
     * @param options command line options
     * @throws Exception on failure
     */
    public static void run(Options options) throws Exception {
        boolean isPub = options.get(OPT_PUB); 

        // Get a topology runtime provider
        DevelopmentProvider tp = new DevelopmentProvider();

        Topology top;
        if (isPub) {
            PublisherApp publisher = new PublisherApp(tp, options);
            top = publisher.buildAppTopology();
        }
        else {
            SubscriberApp subscriber = new SubscriberApp(tp, options);
            top = subscriber.buildAppTopology();
        }
        
        // Submit the app/topology; send or receive the messages.
        System.out.println(
                "Using Kafka cluster at bootstrap.servers="
                + options.get(OPT_BOOTSTRAP_SERVERS)
                + " zookeeper.connect=" + options.get(OPT_ZOOKEEPER_CONNECT)
                + "\n" + (isPub ? "Publishing" : "Subscribing") 
                + " to topic " + options.get(OPT_TOPIC));
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(top);
    }

}
