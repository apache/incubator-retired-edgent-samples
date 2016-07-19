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

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.metrics.Metrics;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * Instruments a topology with a tuple counter on a specified stream.
 */
public class SplitWithMetrics {

    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology(SplitWithMetrics.class.getSimpleName());
        
        Random r = new Random();
        
        TStream<Integer> d  = t.poll(() -> (int)(r.nextGaussian() * 3.0), 
                100, TimeUnit.MILLISECONDS);

        List<TStream<Integer>> splits = d.split(3, tuple -> {
            switch (tuple.intValue()) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 2;
            }
        });

        /* 
         * Insert a metric counter for the zeroes stream.  Note that the 
         * DevelopmentProvider submitter will insert metric counters at 
         * submit time on the output of each oplet, including the counter
         * explicitly inserted below.
         */
        Metrics.counter(splits.get(0)).sink(tuple -> System.out.print("."));

        splits.get(1).sink(tuple -> System.out.print("#"));
        splits.get(2).sink(tuple -> System.out.print("@"));
        
        dtp.submit(t);
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
