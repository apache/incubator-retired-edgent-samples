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

import java.util.Objects;

import org.apache.edgent.analytics.sensors.Range;
import org.apache.edgent.analytics.sensors.Ranges;
import org.apache.edgent.function.Supplier;

/**
 * A Simulated temperature sensor.
 * <p>
 * The sensor starts off with an initial value.
 * Each call to {@link #get()} changes the current value by
 * a random amount between plus/minus a {@code deltaFactor}.
 * The new current value is limited to a {@code tempRange}
 * and then rounded to 1 fractional digit.
 * </p><p>
 * No temperature scale is implied (e.g., Fahrenheit, Kelvin, ...).
 * The {@code double} temperature values are simply generated as described.
 * The user of the class decides how to interpret them.
 * </p><p>
 * Sample use:
 * <pre>{@code
 * Topology t = ...;
 * SimulatedTemperatureSensor tempSensor = new SimulatedTemperatureSensor();
 * TStream<Double> temp = t.poll(tempSensor, 1, TimeUnit.SECONDS);
 * }</pre>
 * @see SimpleSimulatedSensor
 */
public class SimulatedTemperatureSensor implements Supplier<Double> {
    private static final long serialVersionUID = 1L;
    private final SimpleSimulatedSensor sensor;
   
    /**
     * Create a temperature sensor.
     * <p>
     * Same as {@code SimulatedTemperatureSensor(80.0, 
     *              Ranges.closed(28.0, 112.0), 1.0)}
     * </p><p>
     * These default values roughly correspond to normal air temperature
     * in the Fahrenheit scale.
     * </p>
     */
    public SimulatedTemperatureSensor() {
        this(80.0, Ranges.closed(28.0, 112.0), 1.0);
    }
    
    /**
     * Create a temperature sensor.
     * <p>
     * No temperature scale is implied.
     * </p> 
     * @param initialTemp the initial temperature.  Must be within tempRange.
     * @param tempRange maximum sensor value range
     * @param deltaFactor maximum plus/minus change on each {@code get()}.
     *              e.g., 1.0 to limit change to +/- 1.0.
     *              Must be &gt; 0.0
     */
    public SimulatedTemperatureSensor(double initialTemp,
            Range<Double> tempRange, double deltaFactor) {
        Objects.requireNonNull(tempRange, "tempRange");
        if (!tempRange.contains(initialTemp))
            throw new IllegalArgumentException("initialTemp");
        if (deltaFactor <= 0.0)
            throw new IllegalArgumentException("deltaFactor");
        sensor = new SimpleSimulatedSensor(initialTemp, deltaFactor, tempRange);
    }
    
    /** Get the tempRange setting
     * @return the value
     */
    public Range<Double> getTempRange() {
        return sensor.getRange();
    }
    
    /** Get the deltaFactor setting
     * @return the value
     */
    public double getDeltaFactor() {
        return sensor.getDeltaFactor();
    }
    
    /** Get the next sensor value. */
    @Override
    public Double get() {
        return sensor.get();
    }
}
