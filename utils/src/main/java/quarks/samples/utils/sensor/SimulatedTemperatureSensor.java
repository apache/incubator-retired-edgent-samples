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

package quarks.samples.utils.sensor;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Random;

import quarks.analytics.sensors.Range;
import quarks.analytics.sensors.Ranges;
import quarks.function.Supplier;

/**
 * A Simulated temperature sensor.
 * <p>
 * The sensor starts off with an initial value.
 * Each call to {@link #get()} changes the current value by
 * a random amount between plus/minus a {@code deltaFactor}.
 * The new current value is limited to a {@code maxTempRange}.
 * <p>
 * No temperature scale is implied (e.g., Fahrenheit, Kelvin, ...).
 * The {@code double} temperature values are simply generated as described.
 * The user of the class decides how to interpret them.
 * <p>
 * Sample use:
 * <pre>{@code
 * Topology t = ...;
 * SimulatedTemperatureSensor tempSensor = new SimulatedTemperatureSensor();
 * TStream<Double> temp = t.poll(tempSensor, 1, TimeUnit.SECONDS);
 * }</pre>
 */
public class SimulatedTemperatureSensor implements Supplier<Double> {
    private static final long serialVersionUID = 1L;
    private static DecimalFormat df = new DecimalFormat("#.#");
    private Random r = new Random();
    private final Range<Double> maxTempRange;
    private final double deltaFactor;
    private double currentTemp;
   
    /**
     * Create a temperature sensor.
     * <p>
     * Same as {@code SimulatedTemperatureSensor(80.0, 
     *              Ranges.closed(28.0, 112.0), 1.0)}
     * <p>
     * These default values roughly correspond to normal air temperature
     * in the Fahrenheit scale.
     */
    public SimulatedTemperatureSensor() {
        this(80.0, Ranges.closed(28.0, 112.0), 1.0);
    }
    
    /**
     * Create a temperature sensor.
     * <p>
     * No temperature scale is implied. 
     * @param initialTemp the initial temperature.  Must be within maxTempRange.
     * @param maxTempRange maximum sensor value range
     * @param deltaFactor maximum plus/minus change on each {@code get()}.
     *              e.g., 1.0 to limit change to +/- 1.0.
     *              Must be > 0.0
     */
    public SimulatedTemperatureSensor(double initialTemp,
            Range<Double> maxTempRange, double deltaFactor) {
        this.currentTemp = initialTemp;
        this.maxTempRange = maxTempRange;
        this.deltaFactor = deltaFactor;
        Objects.requireNonNull(maxTempRange, "maxTempRange");
        if (!maxTempRange.contains(currentTemp))
            throw new IllegalArgumentException("currentTemp");
        if (deltaFactor <= 0.0)
            throw new IllegalArgumentException("deltaFactor");
    }
    
    /** Get the maxTempRange setting */
    public Range<Double> getMaxTempRange() {
        return maxTempRange;
    }
    
    /** Get the deltaFactor setting */
    public double getDeltaFactor() {
        return deltaFactor;
    }
    
    /** Get the next sensor value. */
    @Override
    public Double get() {
        double delta = 2 * r.nextDouble() - 1.0; // between -1.0 and 1.0
        double newTemp = delta * deltaFactor + currentTemp;
        if (!maxTempRange.contains(newTemp)) {
            newTemp = newTemp > currentTemp
                        ? maxTempRange.upperEndpoint()
                        : maxTempRange.lowerEndpoint();
        }
        currentTemp = Double.valueOf(df.format(newTemp));
        return currentTemp;
    }
}
