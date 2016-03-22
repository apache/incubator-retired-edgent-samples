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

package quarks.samples.topology;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import quarks.console.server.HttpServer;
import quarks.providers.development.DevelopmentProvider;
import quarks.topology.TStream;
import quarks.topology.Topology;

public class DevelopmentSample {
    
    public static void main(String[] args) throws Exception {
        DevelopmentProvider dtp = new DevelopmentProvider();
        
        Topology t = dtp.newTopology("DevelopmentSample");
        
        Random r = new Random();
        
        TStream<Double> d  = t.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);
        
        d.sink(tuple -> System.out.print("."));
        
        dtp.submit(t);
        
        System.out.println(dtp.getServices().getService(HttpServer.class).getConsoleUrl());
    }
}
