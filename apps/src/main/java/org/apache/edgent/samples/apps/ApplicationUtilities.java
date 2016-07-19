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
package org.apache.edgent.samples.apps;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.edgent.analytics.sensors.Range;
import org.apache.edgent.analytics.sensors.Ranges;
import org.apache.edgent.connectors.file.FileStreams;
import org.apache.edgent.connectors.file.FileWriterCycleConfig;
import org.apache.edgent.connectors.file.FileWriterFlushConfig;
import org.apache.edgent.connectors.file.FileWriterPolicy;
import org.apache.edgent.connectors.file.FileWriterRetentionConfig;
import org.apache.edgent.function.Predicate;
import org.apache.edgent.function.Supplier;
import org.apache.edgent.topology.TStream;

/**
 * Some general purpose application configuration driven utilities.
 * <p>
 * Utilities include:
 * <ul>
 * <li>Get a property name for a sensor configuration item</li>
 * <li>Get a Range value for a sensor range item</li>
 * <li>Log a stream</li>
 * <li>Conditionally trace a stream</li>
 * </ul>
 */
public class ApplicationUtilities {
    
    private final Properties props;
    
    public ApplicationUtilities(Properties props) {
        this.props = props;
    }

    private Properties config() {
        return props;
    }
    
    /**
     * Trace a stream to System.out if the sensor id's "label" has been configured
     * to enable tracing.
     * <p>
     * If tracing has not been enabled in the config, the topology will not
     * be augmented to trace the stream.
     *
     * @param <T> Tuple type
     * @param stream the stream to trace
     * @param sensorId the sensor id
     * @param label some unique label
     * @return the input stream
     */
    public <T> TStream<T> traceStream(TStream<T> stream, String sensorId, Supplier<String> label) {
        return traceStream(stream, () -> sensorId+"."+label.get());
    }

    /**
     * Trace a stream to System.out if the "label" has been configured
     * to enable tracing.
     * <p>
     * If tracing has not been enabled in the config, the topology will not
     * be augmented to trace the stream.
     * 
     * @param <T> Tuple type
     * @param stream the stream to trace
     * @param label some unique label
     * @return the input stream
     */
    public <T> TStream<T> traceStream(TStream<T> stream, Supplier<String> label) {
        if (includeTraceStreamOps(label.get())) {
            TStream<?> s = stream.filter(traceTuplesFn(label.get())).tag(label.get()+".trace");
            s.peek(sample -> System.out.println(String.format("%s: %s", label.get(), sample.toString())));
        }
        return stream;
    }
    
    private boolean includeTraceStreamOps(String label) {
        String includesCsv = config().getProperty("stream.tracing.includes.csv", "");
        String includesRegex = config().getProperty("stream.tracing.includes.regex", "");
        String excludesCsv = config().getProperty("stream.tracing.excludes.csv", "");
        String excludesRegex = config().getProperty("stream.tracing.excludes.regex", "");
        
        Set<String> includesSet = new HashSet<>();
        for (String s : includesCsv.split(","))
            includesSet.add(s.trim());
        Set<String> excludesSet = new HashSet<>();
        for (String s : excludesCsv.split(","))
            excludesSet.add(s.trim());
        
        boolean isIncluded = false;
        if (includesSet.contains(label) || label.matches(includesRegex))
            isIncluded = true;
        if (excludesSet.contains(label) || label.matches(excludesRegex))
            isIncluded = false;
        
        return isIncluded;
    }
    
    private <T> Predicate<T> traceTuplesFn(String label) {
        return tuple -> true; // TODO make dynamic config; affected by "label" value
        // check label for match against csv or regex from props
    }
    
    /**
     * Get the property name for a sensor's configuration item.
     * @param sensorId the sensor's id
     * @param label the label for an instance of "kind" (e.g., "tempThreshold")
     * @param kind the kind of configuration item (e.g., "range")
     * @return the configuration property name
     */
    public String getSensorPropertyName(String sensorId, String label, String kind) {
        String name = kind + "." + label;  // kind.label
        if (sensorId!=null && !sensorId.isEmpty())
            name = sensorId + "." + name;  // sensorId.kind.label
        return name;
    }

    private String getSensorConfigValue(String sensorId, String label, String kind) {
        String name = getSensorPropertyName(sensorId, label, kind);
        String val = config().getProperty(name);
        if (val==null)
            throw new IllegalArgumentException("Missing configuration property "+name);
        return val;
    }
    
    /**
     * Get the Range for a sensor range configuration item.
     * @param sensorId the sensor's id
     * @param label the range's label
     * @return the Range
     */
    public Range<Integer> getRangeInteger(String sensorId, String label) {
        String val = getSensorConfigValue(sensorId, label, "range");
        return Ranges.valueOfInteger(val);
    }
    
    /**
     * Get the Range for a sensor range configuration item.
     * @param sensorId the sensor's id
     * @param label the range's label
     * @return the Range
     */
    public Range<Byte> getRangeByte(String sensorId, String label) {
        String val = getSensorConfigValue(sensorId, label, "range");
        return Ranges.valueOfByte(val);
    }
    
    /**
     * Get the Range for a sensor range configuration item.
     * @param sensorId the sensor's id
     * @param label the range's label
     * @return the Range
     */
    public Range<Short> getRangeShort(String sensorId, String label) {
        String val = getSensorConfigValue(sensorId, label, "range");
        return Ranges.valueOfShort(val);
    }
    
    /**
     * Get the Range for a sensor range configuration item.
     * @param sensorId the sensor's id
     * @param label the range's label
     * @return the Range
     */
    public Range<Float> getRangeFloat(String sensorId, String label) {
        String val = getSensorConfigValue(sensorId, label, "range");
        return Ranges.valueOfFloat(val);
    }
    
    /**
     * Get the Range for a sensor range configuration item.
     * @param sensorId the sensor's id
     * @param label the range's label
     * @return the Range
     */
    public Range<Double> getRangeDouble(String sensorId, String label) {
        String val = getSensorConfigValue(sensorId, label, "range");
        return Ranges.valueOfDouble(val);
    }

    /**
     * Log every tuple on the stream using the {@code FileStreams} connector.
     * <p>
     * The logs are added to the directory as specified
     * by the "application.log.dir" property.
     * The directory will be created as needed.
     * <p>
     * The "active" (open / being written) log file name is {@code .<baseName>}.
     * <br>
     * Completed stable logs have a name of {@code <baseName>_YYYYMMDD_HHMMSS}.
     * <p>
     * The log entry format being used is:
     * {@code [<date>] [<eventTag>] <tuple>.toString()}
     * <p>
     * See {@link FileStreams#textFileWriter(TStream, org.apache.edgent.function.Supplier, org.apache.edgent.function.Supplier)}
     * 
     * @param <T> Tuple type
     * @param stream the TStream
     * @param baseName the base log name
     * @param eventTag a tag that gets added to the log entry
     * @return the input stream
     */
    public <T> TStream<T> logStream(TStream<T> stream, String eventTag, String baseName) {
        // Define the writer policy.
        // TODO could make the policy configurable via config()
        FileWriterPolicy<String> policy = new FileWriterPolicy<String>(
                FileWriterFlushConfig.newTimeBasedConfig(2_000/*msec*/), // flush every 2sec
                FileWriterCycleConfig.newFileSizeBasedConfig(10_000),  // new file every 10KB
                FileWriterRetentionConfig.newFileCountBasedConfig(1)   // retain 1 file
                );
        
        // Compose the base file pathname
        File dir = new File(config().getProperty("application.log.dir"));
        String basePathname = new File(dir, baseName).toString();
         
        // Transform the stream to a TStream<String> of string log entry values
        TStream<String> stringEntries = stream.map(sample -> String.format("[%s] [%s] %s", new Date().toString(), eventTag, sample.toString()))
                .tag(baseName+".log");

        // Use the FileStreams connector to write the logs.
        //
        // A hack for getting the log directories created at runtime
        // TODO add another policy thing... or simply make textFileWriter do it?
        //
        FileStreams.textFileWriter(stringEntries,
                () -> { if (!dir.exists()) dir.mkdirs();
                        return basePathname;
                      },
                () -> policy);
        
        return stream;
    }

}
