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
package org.apache.edgent.samples.connectors.mqtt;

import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_ACTION_TIMEOUT_MILLIS;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_CLEAN_SESSION;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_CLIENT_ID;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_CN_TIMEOUT_SEC;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_IDLE_RECONNECT_INTERVAL_SEC;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_IDLE_TIMEOUT_SEC;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_KEY_STORE;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_KEY_STORE_PASSWORD;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_PASSWORD;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_PUB;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_SERVER_URI;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_TOPIC;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_TRUST_STORE;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_TRUST_STORE_PASSWORD;
import static org.apache.edgent.samples.connectors.mqtt.MqttClient.OPT_USER_ID;

import org.apache.edgent.connectors.mqtt.MqttConfig;
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

        // System.setProperty("javax.net.debug", "ssl"); // or "all"; "help" for full list
        
        // Submit the app/topology; send or receive the messages.
        System.out.println(
                "Using MQTT broker at " + options.get(OPT_SERVER_URI)
                + "\n" + (isPub ? "Publishing" : "Subscribing")
                + " to topic "+options.get(OPT_TOPIC));
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(top);
    }

    /**
     * Build a MqttConfig broker connector configuration.
     * @param options command line options
     * @return the connector configuration
     */
    static MqttConfig newConfig(Options options) {
        // Only the serverURI is required.  Everything else is optional.
        MqttConfig config = new MqttConfig(options.get(OPT_SERVER_URI),
                                            options.get(OPT_CLIENT_ID));
        
        if (options.get(OPT_CLEAN_SESSION) != null)
            config.setCleanSession(options.get(OPT_CLEAN_SESSION));
        if (options.get(OPT_CN_TIMEOUT_SEC) != null)
            config.setConnectionTimeout(options.get(OPT_CN_TIMEOUT_SEC));
        if (options.get(OPT_ACTION_TIMEOUT_MILLIS) != null)
            config.setActionTimeToWaitMillis(options.get(OPT_ACTION_TIMEOUT_MILLIS));
        if (options.get(OPT_IDLE_TIMEOUT_SEC) != null)
            config.setIdleTimeout(options.get(OPT_IDLE_TIMEOUT_SEC));
        if (options.get(OPT_IDLE_RECONNECT_INTERVAL_SEC) != null)
            config.setSubscriberIdleReconnectInterval(options.get(OPT_IDLE_RECONNECT_INTERVAL_SEC));
        if (options.get(OPT_USER_ID) != null)
            config.setUserName(options.get(OPT_USER_ID));
        if (options.get(OPT_PASSWORD) != null)
            config.setPassword(((String)options.get(OPT_PASSWORD)).toCharArray());
        if (options.get(OPT_TRUST_STORE) != null)
            config.setTrustStore(options.get(OPT_TRUST_STORE));
        if (options.get(OPT_TRUST_STORE_PASSWORD) != null)
            config.setTrustStore(options.get(OPT_TRUST_STORE_PASSWORD));
        if (options.get(OPT_KEY_STORE) != null)
            config.setKeyStore(options.get(OPT_KEY_STORE));
        if (options.get(OPT_KEY_STORE_PASSWORD) != null)
            config.setTrustStore(options.get(OPT_KEY_STORE_PASSWORD));
        return config;
    }

}
