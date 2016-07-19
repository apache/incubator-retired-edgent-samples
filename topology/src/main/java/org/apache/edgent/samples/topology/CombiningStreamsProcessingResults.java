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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.samples.utils.sensor.HeartMonitorSensor;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * Applying different processing against a set of streams and combining the
 * resulting streams into a single stream.
 *
 *  @see HeartMonitorSensor
 */
public class CombiningStreamsProcessingResults {
    /**
     * Polls a simulated heart monitor to periodically obtain blood pressure readings.
     * Splits the readings by blood pressure category into separate streams.
     * Applies different processing on each stream to generate alert streams.
     * Combines the alert streams into a single stream and prints the alerts.
     *
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {
        HeartMonitorSensor monitor = new HeartMonitorSensor();

        DirectProvider dp = new DevelopmentProvider();

        System.out.println(dp.getServices().getService(HttpServer.class).getConsoleUrl());

        Topology top = dp.newTopology("heartMonitor");

        // Generate a stream of heart monitor readings
        TStream<Map<String, Integer>> readings = top
                .poll(monitor, 1, TimeUnit.MILLISECONDS)
                .filter(tuple -> tuple.get("Systolic") > 50 && tuple.get("Diastolic") > 30)
                .filter(tuple -> tuple.get("Systolic") < 200 && tuple.get("Diastolic") < 130);

        // Split the stream by blood pressure category
        List<TStream<Map<String, Integer>>> categories = readings.split(6, tuple -> {
            int s = tuple.get("Systolic");
            int d = tuple.get("Diastolic");
            if (s < 120 && d < 80) {
                // Normal
                return 0;
            } else if ((s >= 120 && s <= 139) || (d >= 80 && d <= 89)) {
                // Prehypertension
                return 1;
            } else if ((s >= 140 && s <= 159) || (d >= 90 && d <= 99)) {
                // High Blood Pressure (Hypertension) Stage 1
                return 2;
            } else if ((s >= 160 && s <= 179) || (d >= 100 && d <= 109)) {
                // High Blood Pressure (Hypertension) Stage 2
                return 3;
            } else if (s >= 180 && d >= 110)  {
                // Hypertensive Crisis
                return 4;
            } else {
                // Invalid
                return -1;
            }
        });

        // Get each individual stream
        TStream<Map<String, Integer>> normal = categories.get(0).tag("normal");
        TStream<Map<String, Integer>> prehypertension = categories.get(1).tag("prehypertension");
        TStream<Map<String, Integer>> hypertension_stage1 = categories.get(2).tag("hypertension_stage1");
        TStream<Map<String, Integer>> hypertension_stage2 = categories.get(3).tag("hypertension_stage2");
        TStream<Map<String, Integer>> hypertensive = categories.get(4).tag("hypertensive");

        // Perform analytics on each stream and generate alerts for each blood pressure category

        // Category: Normal
        TStream<String> normalAlerts = normal
                .filter(tuple -> tuple.get("Systolic") > 80 && tuple.get("Diastolic") > 50)
                .tag("normal")
                .map(tuple -> {
                    return "All is normal. BP is " + tuple.get("Systolic") + "/" +
                            tuple.get("Diastolic") + ".\n"; })
                .tag("normal");

        // Category: Prehypertension category
        TStream<String> prehypertensionAlerts = prehypertension
                .map(tuple -> {
                    return "At high risk for developing hypertension. BP is " +
                            tuple.get("Systolic") + "/" + tuple.get("Diastolic") + ".\n"; })
                .tag("prehypertension");

        // Category: High Blood Pressure (Hypertension) Stage 1
        TStream<String> hypertension_stage1Alerts = hypertension_stage1
                .map(tuple -> {
                    return "Monitor closely, patient has high blood pressure. " +
                           "BP is " + tuple.get("Systolic") + "/" + tuple.get("Diastolic") + ".\n"; })
                .tag("hypertension_stage1")
                .modify(tuple -> "High Blood Pressure (Hypertension) Stage 1\n" + tuple)
                .tag("hypertension_stage1");

        // Category: High Blood Pressure (Hypertension) Stage 2
        TStream<String> hypertension_stage2Alerts = hypertension_stage2
                .filter(tuple -> tuple.get("Systolic") >= 170 && tuple.get("Diastolic") >= 105)
                .tag("hypertension_stage2")
                .peek(tuple ->
                    System.out.println("BP: " + tuple.get("Systolic") + "/" + tuple.get("Diastolic")))
                .map(tuple -> {
                    return "Warning! Monitor closely, patient is at risk of a hypertensive crisis!\n"; })
                .tag("hypertension_stage2")
                .modify(tuple -> "High Blood Pressure (Hypertension) Stage 2\n" + tuple)
                .tag("hypertension_stage2");

        // Category: Hypertensive Crisis
        TStream<String> hypertensiveAlerts = hypertensive
                .filter(tuple -> tuple.get("Systolic") >= 180)
                .tag("hypertensive")
                .peek(tuple ->
                    System.out.println("BP: " + tuple.get("Systolic") + "/" + tuple.get("Diastolic")))
                .map(tuple -> { return "Emergency! See to patient immediately!\n"; })
                .tag("hypertensive")
                .modify(tuple -> tuple.toUpperCase())
                .tag("hypertensive")
                .modify(tuple -> "Hypertensive Crisis!!!\n" + tuple)
                .tag("hypertensive");

        // Additional processing for these streams could go here. In this case, union two streams
        // to obtain a single stream containing alerts from the normal and prehypertension alert streams.
        TStream<String> normalAndPrehypertensionAlerts = normalAlerts.union(prehypertensionAlerts);

        // Set of streams containing alerts from the other categories
        Set<TStream<String>> otherAlerts = new HashSet<>();
        otherAlerts.add(hypertension_stage1Alerts);
        otherAlerts.add(hypertension_stage2Alerts);
        otherAlerts.add(hypertensiveAlerts);

        // Union a stream with a set of streams to obtain a single stream containing alerts from
        // all alert streams
        TStream<String> allAlerts = normalAndPrehypertensionAlerts.union(otherAlerts);

        // Terminate the stream by printing out alerts from all categories
        allAlerts.sink(tuple -> System.out.println(tuple));

        dp.submit(top);
    }
}
