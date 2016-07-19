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

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * Illustrates tagging TStreams with string labels.
 */
public class StreamTags {
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("StreamTags");
        
        // Tag the source stream with 
        Random r = new Random();
        TStream<Double> d  = t.poll(() -> (r.nextDouble() * 3), 
                100, TimeUnit.MILLISECONDS).tag("dots", "hashes", "ats");

        List<TStream<Double>> splits = d.split(3, tuple -> {
            switch (tuple.intValue()) {
            case 0:
                return 0;
            case 1:
                return 1;
            default:
                return 2;
            }
        });

        splits.get(0).tag("dots").sink(tuple -> System.out.print("."));
        splits.get(1).tag("hashes").sink(tuple -> System.out.print("#"));
        splits.get(2).tag("ats").sink(tuple -> System.out.print("@"));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
