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
package org.apache.edgent.samples.utils.metrics;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.metrics.Metrics;
import org.apache.edgent.metrics.MetricsSetup;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import com.codahale.metrics.MetricRegistry;

public class PeriodicSourceWithMetrics {
    public static void main(String[] args) throws InterruptedException {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("PeriodicSource");

        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Testing Peek
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));

        // Measure the tuple count for the gaussian TStream
        gaussian = Metrics.counter(gaussian);
        
        // A filter
        gaussian = gaussian.filter(g -> g > 0.5);

        // Measure tuple arrival rate after filtering
        gaussian = Metrics.rateMeter(gaussian);

        // A transformation
        TStream<String> gs = gaussian.map(g -> "G:" + g + ":");
        gs.print();

        // Initialize the metrics service
        MetricRegistry metrics = new MetricRegistry();
        
        // Start metrics JMX reporter
        MetricsSetup.withRegistry(tp.getServices(), metrics).startJMXReporter(
                PeriodicSourceWithMetrics.class.getName());

        // Submit the topology
        tp.submit(t);
    }
}
