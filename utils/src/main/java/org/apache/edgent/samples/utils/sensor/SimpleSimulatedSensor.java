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

import org.apache.commons.math3.util.Precision;
import org.apache.edgent.analytics.sensors.Range;
import org.apache.edgent.function.Supplier;

/**
 * A simple simulated sensor.
 * <p>
 * The sensor starts off with an initial value.
 * Each call to {@link #get()} changes the current value by
 * a random amount between plus/minus {@code deltaFactor}.
 * The new current value is limited to a {@code range}
 * and then rounded to 1 fractional digit. 
 * See {@link #setNumberFractionalDigits(int)}.
 * </p><p>
 * Sample use:
 * <pre>{@code
 * Topology t = ...;
 * // a miles-per-gallon sensor
 * SimpleSimulatedSensor avgMpgSensor = new SimpleSimulatedSensor(10.5, 0.4,
 *                                          Ranges<Double>.closed(7.0,14.0));
 * TStream<Double> avgMpg = t.poll(avgMpgSensor, 1, TimeUnit.SECONDS);
 * 
 * // an integer valued sensor
 * SimpleSimulatedSensor doubleSensor = new SimpleSimulatedSensor();
 * TStream<Integer> intSensor = t.poll(() -> doubleSensor.get().intValue(),
 *                                          1, TimeUnit.SECONDS);
 * }</pre>
 */
public class SimpleSimulatedSensor implements Supplier<Double> {
    private static final long serialVersionUID = 1L;
    private int numFracDigits;
    private Random r = new Random();
    private final Range<Double> range;
    private final double deltaFactor;
    private double currentValue;
   
    /**
     * Create a sensor.
     * <p>
     * Same as {@code SimpleSimulatedSensor(0.0, 1.0, null)};
     * </p>
     */
    public SimpleSimulatedSensor() {
        this(0.0, 1.0, null);
    }
    
    /**
     * Create a sensor.
     * <p>
     * Same as {@code SimpleSimulatedSensor(initialValue, 1.0, null)};
     * </p>
     * @param initialValue the initial value
     */
    public SimpleSimulatedSensor(double initialValue) {
        this(initialValue, 1.0, null);
    }
    
    /**
     * Create a sensor.
     * 
     * <p>
     * Same as {@code SimpleSimulatedSensor(initialValue, deltaFactor, null)};
     * </p>
     * @param initialValue the initial value.
     * @param deltaFactor maximum plus/minus change on each {@code get()}.
     *              e.g., 1.0 to limit change to +/- 1.0.
     *              Must be &gt; 0.0
     */
    public SimpleSimulatedSensor(double initialValue, double deltaFactor) {
        this(initialValue, deltaFactor, null);
    }
    
    /**
     * Create a sensor.
     * 
     * @param initialValue the initial value.  Must be within range.
     * @param deltaFactor maximum plus/minus change on each {@link #get()}.
     *              e.g., 1.0 to limit change to +/- 1.0.
     *              Must be &gt; 0.0
     * @param range maximum sensor value range. Unlimited if null.
     */
    public SimpleSimulatedSensor(double initialValue,
            double deltaFactor, Range<Double> range) {
        if (range!=null && !range.contains(initialValue))
            throw new IllegalArgumentException("initialValue");
        if (deltaFactor <= 0.0)
            throw new IllegalArgumentException("deltaFactor");
        this.currentValue = initialValue;
        this.deltaFactor = deltaFactor;
        this.range = range;
        setNumberFractionalDigits(1);
    }
    
    /**
     * Set number of fractional digits to round sensor values to.
     * <p>
     * This class offers rounding as a convenience and because
     * ancestors of this implementation had such a scheme.
     * </p>
     * @param numFracDigits  if &lt;= 0, no rounding will be performed
     */
    public void setNumberFractionalDigits(int numFracDigits) {
        this.numFracDigits = numFracDigits;
        if (numFracDigits <= 0) {
            this.numFracDigits = 0;
        }
    }
    
    /** Get the number of fractional digits setting
     * @return the value
     */
    public int getNumberFractionalDigits() {
        return numFracDigits;
    }
    
    /** Get the range setting
     * @return the value
     */
    public Range<Double> getRange() {
        return range;
    }
    
    /** Get the deltaFactor setting
     * @return the value
     */
    public double getDeltaFactor() {
        return deltaFactor;
    }
    
    /** Get the next sensor value as described in the class documentation. */
    @Override
    public Double get() {
        double delta = 2 * r.nextDouble() - 1.0; // between -1.0 and 1.0
        double nextValue = currentValue + delta * deltaFactor;
        if (range!=null && !range.contains(nextValue)) {
            nextValue = nextValue > currentValue
                        ? range.upperEndpoint()
                        : range.lowerEndpoint();
        }
        currentValue = Precision.round(nextValue, numFracDigits);
        return currentValue;
    }
}
