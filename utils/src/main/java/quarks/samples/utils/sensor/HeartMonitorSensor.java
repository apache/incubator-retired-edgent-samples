/*
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2015,2016
*/

package quarks.samples.utils.sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import quarks.function.Supplier;

/**
 * Streams of simulated heart monitor sensors.
 *
 */
public class HeartMonitorSensor implements Supplier<Map<String,Integer>> {
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
