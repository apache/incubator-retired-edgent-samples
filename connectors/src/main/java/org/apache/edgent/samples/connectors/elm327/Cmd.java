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
package org.apache.edgent.samples.connectors.elm327;

import java.io.IOException;
import java.io.OutputStream;

import com.google.gson.JsonObject;

/**
 * ELM327 and OBD-II command interface.
 *
 */
public interface Cmd {
    /**
     * Key ({@value}) for PID identifier in JSON result.
     */
    String PID = "pid";

    /**
     * Key ({@value}) for timestamp in JSON result. Timestamp value is the
     * number of milliseconds since the 1907 epoch.
     */
    String TS = "ts";
    
    /**
     * Key ({@value}) for the returned value in JSON result.
     * May not be present.
     */
    String VALUE = "value";

    /**
     * How the command is written to the serial port.
     * 
     * @param out
     *            OutputStream to write bytes to.
     * @throws IOException
     *             Exception writing bytes.
     */
    void writeCmd(OutputStream out) throws IOException;

    /**
     * Process the reply into a result.
     * 
     * @param result
     *            JSON object to populate with the result.
     * @param reply
     *            Bytes that were returned from the command execution.
     *            
     * @return {@code true} result is valid, {@code false} otherwise.
     */
    boolean result(JsonObject result, byte[] reply);

    /**
     * Unique identifier of the command.
     * 
     * @return Unique identifier of the command.
     */
    String id();
}
