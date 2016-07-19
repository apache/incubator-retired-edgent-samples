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

import java.util.concurrent.TimeUnit;

import org.apache.edgent.connectors.serial.SerialDevice;
import org.apache.edgent.function.Supplier;
import org.apache.edgent.samples.connectors.elm327.runtime.CommandExecutor;
import org.apache.edgent.topology.TStream;

import com.google.gson.JsonArray;

/**
 * Streams fetching OBD-II data from an ELM327 through
 * a serial device.
 *
 * @see <a href="https://en.wikipedia.org/wiki/ELM327">ELM327</a>
 */
public class Elm327Streams {
	
    /**
     * Periodically execute a number of ELM327 commands.
     * Each tuple on the returned stream is a JSON array containing
     * the result for each command.
     * <BR>
     * Each result is a JSON object containing the
     * {@link Cmd#id() command identifier} with key {@link Cmd#PID pid}
     * and any result set by the individual command, typically with
     * the key {@link Cmd#VALUE value}.
     * 
     * @param device Serial device the ELM327 is connected to.
     * @param period Period to poll.
     * @param unit Unit of {@code period}.
     * @param cmds Commands to execute.
     * @return Stream containing the results of the command exections.
     */
	public static TStream<JsonArray> poll(SerialDevice device, long period, TimeUnit unit, Cmd ... cmds) {
		
		Supplier<JsonArray> data = device.getSource(
				port ->
		{
			JsonArray array = new JsonArray();
			for (Cmd cmd : cmds) {
				array.add(CommandExecutor.execute(cmd, port.getOutput(), port.getInput()));
			}
			return array;
			
		});
		
		return device.topology().poll(data, period, unit);

	}
}
