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

package org.apache.edgent.samples.utils.sensor;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import com.google.gson.JsonObject;

/**
 * Streams of simulated sensors.
 *
 */
public class SimulatedSensors {

    /**
     * Create a stream of simulated bursty sensor readings.
     * 
     * Simulation of reading a sensor every 100ms with the readings
     * generally falling below 2.0 (absolute) but randomly have
     * prolonged bursts of higher values.
     * 
     * Each tuple is a JSON object containing:
     * <UL>
     * <LI>{@code name} - Name of the sensor from {@code name}.</LI>
     * <LI>{@code reading} - Value.</LI>
     * </UL>
     * 
     * @param topology Topology to be added to.
     * @param name Name of the sensor in the JSON output.
     * @return Stream containing bursty data.
     */
    public static TStream<JsonObject> burstySensor(Topology topology, String name) {

        Random r = new Random();

        TStream<Double> sensor = topology.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);

        boolean[] abnormal = new boolean[1];
        int[] count = new int[1];
        double[] delta = new double[1];
        sensor = sensor.modify(t -> {
            if (abnormal[0] || r.nextInt(100) < 4) {
                if (!abnormal[0]) {
                    delta[0] = 0.5 + 2 * r.nextGaussian();
                    count[0] = 5 + r.nextInt(20);
                    abnormal[0] = true;
                }
                count[0]--;
                if (count[0] <= 0)
                    abnormal[0] = false;
                return t + delta[0];
            } else
                return t;
        });

        sensor = sensor.filter(t -> Math.abs(t) > 1.5);

        return sensor.map(t -> {
            JsonObject j = new JsonObject();
            j.addProperty("name", name);
            j.addProperty("reading", t);
            return j;
        });

    }

}
