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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.edgent.function.Supplier;

/**
 * Streams of simulated heart monitor sensors.
 *
 */
public class HeartMonitorSensor implements Supplier<Map<String,Integer>> {
    private static final long serialVersionUID = 1L;
    // Initial blood pressure
    public Integer currentSystolic = 115;
    public Integer currentDiastolic = 75;
    Random rand;

    public HeartMonitorSensor() {
        rand = new Random();
    }

    /**
     * Every call to this method returns a map containing a random systolic
     * pressure and a random diastolic pressure.
     */
    @Override
    public Map<String, Integer> get() {
        // Change the current pressure by some random amount between -2 and 2
        Integer newSystolic = rand.nextInt(2 + 1 + 2) - 2 + currentSystolic;
        currentSystolic = newSystolic;

        Integer newDiastolic = rand.nextInt(2 + 1 + 2) - 2 + currentDiastolic;
        currentDiastolic = newDiastolic;

        Map<String, Integer> pressures = new HashMap<String, Integer>();
        pressures.put("Systolic", currentSystolic);
        pressures.put("Diastolic", currentDiastolic);
        return pressures;
    }
}
