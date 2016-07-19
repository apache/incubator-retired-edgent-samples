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
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * This application simulates a crash and terminates the JVM after processing
 * a preset number of tuples. This application is used in conjunction with a 
 * monitoring script to demonstrate the restart of a JVM which has terminated
 * because of an Edgent application crash.
 */
public class TerminateAfterNTuples {
    /** The application will terminate the JVM after this tuple count */
    public final static int TERMINATE_COUNT = 15;
    
    public static void main(String[] args) throws Exception {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("PeriodicSource");

        // Since this is the Direct provider the graph can access
        // objects created while the topology is being defined
        // (in this case the Random object r).
        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Program termination
        AtomicInteger count = new AtomicInteger(0);
        gaussian = gaussian.peek(g -> {
            if (count.incrementAndGet() >= TERMINATE_COUNT) {
                System.err.println("The JVM terminates after processing " + 
                        TERMINATE_COUNT + " tuples");
                System.exit(1);
            }
        });

        // Peek at the value on the Stream printing it to System.out
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));

        tp.submit(t);
    }
}
