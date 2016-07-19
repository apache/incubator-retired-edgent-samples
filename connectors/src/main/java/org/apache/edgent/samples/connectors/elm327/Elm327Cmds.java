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
import java.nio.charset.StandardCharsets;

import org.apache.edgent.connectors.serial.SerialDevice;
import org.apache.edgent.samples.connectors.elm327.runtime.CommandExecutor;

import com.google.gson.JsonObject;

/**
 * ELM327 commands.
 * 
 * 
 */
public enum Elm327Cmds implements Cmd {

    INIT("ATZ"),
    ECHO_OFF("ATE0"),
    PROTOCOL_3("ATSP3"),
    PROTOCOL_5("ATSP5"),
    BYPASS_INIT("ATBI"),
    FAST_INIT("ATFI"),
    SLOW_INIT("ATSI"),;

    private byte[] cmd;

    Elm327Cmds(String code) {
        cmd = (code + "\r").getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public void writeCmd(OutputStream out) throws IOException {
        out.write(cmd);
    }

    @Override
    public boolean result(JsonObject result, byte[] data) {
        return true;
    }

    @Override
    public String id() {
        return name();
    }
    
    /**
     * Initialize the ELM327 to a specific protocol.
     * @param device Serial device the ELM327 is connected to.
     * @param protocol OBD-II protocol to initialize to.
     */
    public static void initializeProtocol(SerialDevice device, Elm327Cmds protocol) {
        device.setInitializer(port -> CommandExecutor.initialize(protocol, port.getOutput(), port.getInput()));
    }

}
