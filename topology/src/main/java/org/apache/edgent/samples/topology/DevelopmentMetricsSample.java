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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.metrics.Metrics;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

public class DevelopmentMetricsSample {

    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        DevelopmentProvider dtp2 = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentMetricsSample");
        Topology t2 = dtp2.newTopology("another one");
        
        Random r = new Random();
        Random r2 = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);
        
        TStream<Double> gaussian2 = t2.poll(() -> r2.nextGaussian(), 1, TimeUnit.SECONDS);

        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);
        
        // Measure tuple arrival rate after filtering
        gaussian = Metrics.rateMeter(gaussian);

        // A transformation
        @SuppressWarnings("unused")
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        @SuppressWarnings("unused")
        TStream<String> gs2 = gaussian2.map(g -> "G:" + g + ":");
        
        dtp.submit(t);
        dtp2.submit(t2);
        
        System.out.println(dtp2.getServices().getService(HttpServer.class).getConsoleUrl());
        
        Thread.sleep(1000000);
    }
}
