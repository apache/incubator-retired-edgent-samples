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
package org.apache.edgent.samples.apps.sensorAnalytics;

import static org.apache.edgent.analytics.math3.stat.Statistic.MAX;
import static org.apache.edgent.analytics.math3.stat.Statistic.MEAN;
import static org.apache.edgent.analytics.math3.stat.Statistic.MIN;
import static org.apache.edgent.analytics.math3.stat.Statistic.STDDEV;
import static org.apache.edgent.samples.apps.JsonTuples.KEY_ID;
import static org.apache.edgent.samples.apps.JsonTuples.KEY_READING;
import static org.apache.edgent.samples.apps.JsonTuples.KEY_TS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.math3.util.Pair;
import org.apache.edgent.analytics.sensors.Range;
import org.apache.edgent.analytics.sensors.Ranges;
import org.apache.edgent.connectors.iot.QoS;
import org.apache.edgent.function.Supplier;
import org.apache.edgent.samples.apps.JsonTuples;
import org.apache.edgent.samples.utils.sensor.PeriodicRandomSensor;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;
import org.apache.edgent.topology.plumbing.PlumbingStreams;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Analytics for "Sensor1".
 * <p>
 * This sample demonstrates some common continuous sensor analytic themes.
 * <p>
 * In this case we have a simulated sensor producing 1000 samples per second
 * of an integer type in the range of 0-255.
 * <p>
 * The processing pipeline created is roughly:
 * <ul>
 * <li>Batched Data Reduction - reduce the sensor's 1000 samples per second
 *     down to 1 sample per second simple statistical aggregation of the readings.
 *     </li>
 * <li>Compute historical information - each 1hz sample is augmented
 *     with a 30 second trailing average of the 1hz readings.
 *     </li>
 * <li>Threshold detection - each 1hz sample's value is compared
 *     against a target range and outliers are identified.
 *     </li>
 * <li>Local logging - outliers are logged to a local file
 *     </li>
 * <li>Publishing results to a MQTT broker:
 *     <ul>
 *     <li>when enabled, invdividual outliers are published.</li>
 *     <li>Every 30 seconds a list of the last 10 outliers is published.</li>
 *     </ul>
 *     </li>
 * </ul>
 * <p>
 * The sample also demonstrates:
 * <ul>
 * <li>Dynamic configuration control - subscribe to a MQTT broker
 *     to receive commands to adjust the threshold detection range value. 
 *     </li>
 * <li>Generally, the configuration of the processing is driven via an
 *     external configuration description.
 *     </li>
 * <li>Conditional stream tracing - configuration controlled inclusion of tracing.
 *     </li>
 * <li>Use of {@link TStream#tag(String...)} to improve information provided by
 *     the Edgent DevelopmentProvider console.</li>
 * </ul>
 */
public class Sensor1 {
    private final SensorAnalyticsApplication app;
    private final Topology t;
    private final String sensorId = "sensor1";

    public Sensor1(Topology t, SensorAnalyticsApplication app) {
        this.t = t;
        this.app = app;
    }
    
    /**
     * Add the sensor's analytics to the topology.
     */
    public void addAnalytics() {

        // Need synchronization for set/get of dynamically changeable values.
        AtomicReference<Range<Integer>> range = new AtomicReference<>();
        AtomicReference<Boolean> isPublish1hzOutsideRange = new AtomicReference<>();
        
        // Initialize the controls
        range.set(app.utils().getRangeInteger(sensorId, "outside1hzMeanRange"));
        isPublish1hzOutsideRange.set(false);
        
        // Handle the sensor's device commands
        app.mqttDevice().commands(commandId("set1hzMeanRangeThreshold"))
            .tag(commandId("set1hzMeanRangeThresholdCmd"))
            .sink(jo -> {
                    Range<Integer> newRange = Ranges.valueOfInteger(getCommandValue(jo));
                    System.out.println("===== Changing range to "+newRange+" ======");
                    range.set(newRange);
                });
        app.mqttDevice().commands(commandId("setPublish1hzOutsideRange"))
            .tag(commandId("setPublish1hzOutsideRangeCmd"))
            .sink(jo -> {
                    Boolean b = new Boolean(getCommandValue(jo));
                    System.out.println("===== Changing isPublish1hzOutsideRange to "+b+" ======");
                    isPublish1hzOutsideRange.set(b);
                });
        
        // Create a raw simulated sensor stream of 1000 tuples/sec.
        // Each tuple is Pair<Long timestampMsec, sensor-reading (0..255)>.
        PeriodicRandomSensor simulatedSensorFactory = new PeriodicRandomSensor();
        TStream<Pair<Long,Integer>> raw1khz = 
                simulatedSensorFactory.newInteger(t, 1/*periodMsec*/, 255)
                .tag("raw1khz");
        traceStream(raw1khz, "raw1khz");
        
        // Wrap the raw sensor reading in a JsonObject for convenience.
        TStream<JsonObject> j1khz = JsonTuples.wrap(raw1khz, sensorId)
                .tag("j1khz");
        traceStream(j1khz, "j1khz");
        
        // Data-reduction: reduce 1khz samples down to
        // 1hz aggregate statistics samples.
        TStream<JsonObject> j1hzStats = j1khz.last(1000, JsonTuples.keyFn())
                .batch(JsonTuples.statistics(MIN, MAX, MEAN, STDDEV))
                .tag("1hzStats");
        
        // Create a 30 second sliding window of average trailing Mean values
        // and enrich samples with that information.
        j1hzStats = j1hzStats.last(30, JsonTuples.keyFn()).aggregate(
            (samples, key) -> {
                // enrich and return the most recently added tuple
                JsonObject jo = samples.get(samples.size()-1);
                double meanSum = 0;
                for (JsonObject js : samples) {
                    meanSum += JsonTuples.getStatistic(js, MEAN).getAsDouble();
                }
                jo.addProperty("AvgTrailingMean", Math.round(meanSum / samples.size()));
                jo.addProperty("AvgTrailingMeanCnt", samples.size());
                return jo;
            })
            .tag("1hzStats.enriched");
        traceStream(j1hzStats, "j1hzStats");

        // Detect 1hz samples whose MEAN value are
        // outside the configuration specified range.
        TStream<JsonObject> outside1hzMeanRange = j1hzStats.filter(
                sample -> {
                    int value = JsonTuples.getStatistic(sample, MEAN).getAsInt();
                    return !range.get().contains(value);
                })
                .tag("outside1hzMeanRange");
        traceStream(outside1hzMeanRange, () -> "outside1hzMeanRange"+range.get()); 
        
        // Log every outside1hzMeanRange event
        app.utils().logStream(outside1hzMeanRange, "ALERT", "outside1hzMeanRange");
        
        // Conditionally publish every outside1hzMeanRange event.
        // Use a pressureReliever to prevent backpressure if the broker
        // can't be contacted.
        // TODO enhance MqttDevice with configurable reliever. 
        app.mqttDevice().events(
                PlumbingStreams.pressureReliever(
                    outside1hzMeanRange.filter(tuple -> isPublish1hzOutsideRange.get())
                                       .tag("outside1hzMeanRangeEvent.conditional"),
                    tuple -> 0, 30).tag("outside1hzMeanRangeEvent.pressureRelieved"),
                app.sensorEventId(sensorId, "outside1hzMeanRangeEvent"), QoS.FIRE_AND_FORGET);
        
        // Demonstrate periodic publishing of a sliding window if
        // something changed since it was last published.
        periodicallyPublishLastNInfo(outside1hzMeanRange, 10, 30,
                "periodicLastOutsideRangeEvent");
        
        // TODO histogram: #alerts over the last 8hr

    }
    
    /**
     * Periodically publish the lastN on a stream.
     * @param stream tuples to 
     * @param count sliding window size "lastN"
     * @param nSec publish frequency
     * @param event sensor's publish event label
     */
    private void periodicallyPublishLastNInfo(TStream<JsonObject> stream, 
            int count, int nSec, String event) {

        // Demonstrate periodic publishing of a sliding window if
        // something changed since it was last published.

        // Maintain a sliding window of the last N tuples.
        // TODO today, windows don't provide "anytime" access to their collection
        // so maintain our own current copy of the collection that we can
        // access it when needed.
        // 
        List<JsonObject> lastN = Collections.synchronizedList(new ArrayList<>());
        stream.last(count, JsonTuples.keyFn())
            .aggregate((samples, key) -> samples)
            .tag(event+".lastN")
            .sink(samples -> {
                    // Capture the new list/window.  
                    synchronized(lastN) {
                        lastN.clear();
                        lastN.addAll(samples);
                    }
                });
    
        // Publish the lastN (with trimmed down info) every nSec seconds
        // if anything changed since the last publish.
        TStream<JsonObject> periodicLastN = 
                t.poll(() -> 1, nSec, TimeUnit.SECONDS).tag(event+".trigger")
                .filter(trigger -> !lastN.isEmpty()).tag(event+".changed")
                .map(trigger -> {
                    synchronized(lastN) {
                        // create a single JsonObject with the list
                        // of reduced-content samples
                        JsonObject jo = new JsonObject();
                        jo.addProperty(KEY_ID, sensorId);
                        jo.addProperty(KEY_TS, System.currentTimeMillis());
                        jo.addProperty("window", count);
                        jo.addProperty("pubFreqSec", nSec);
                        JsonArray ja = new JsonArray();
                        jo.add("lastN", ja);
                        for (JsonObject j : lastN) {
                            JsonObject jo2 = new JsonObject();
                            ja.add(jo2);
                            jo2.add(KEY_TS, j.get(KEY_TS));
                            // reduce size: include only 2 significant digits
                            jo2.addProperty(KEY_READING, String.format("%.2f", 
                                JsonTuples.getStatistic(j, MEAN).getAsDouble()));
                        }
                        lastN.clear();
                        return jo;
                    }
                })
                .tag(event);

        traceStream(periodicLastN, event);

        // Use a pressureReliever to prevent backpressure if the broker
        // can't be contacted.
        // TODO enhance MqttDevice with configurable reliever. 
        app.mqttDevice().events(
                PlumbingStreams.pressureReliever(periodicLastN, tuple -> 0, 30)
                    .tag(event+".pressureRelieved"),
                app.sensorEventId(sensorId, event), QoS.FIRE_AND_FORGET);
    }
    
    private String commandId(String commandId) {
        return app.commandId(sensorId, commandId);
    }
    
    private String getCommandValue(JsonObject jo) {
        return app.getCommandValueString(jo);
    }
    
    private <T> TStream<T> traceStream(TStream<T> stream, String label) {
        return traceStream(stream, () -> label); 
    }
    
    private <T> TStream<T> traceStream(TStream<T> stream, Supplier<String> label) {
        return app.utils().traceStream(stream, sensorId, label); 
    }
}
