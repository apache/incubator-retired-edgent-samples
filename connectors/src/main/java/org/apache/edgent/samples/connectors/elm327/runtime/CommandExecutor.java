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
package org.apache.edgent.samples.connectors.elm327.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.edgent.samples.connectors.elm327.Cmd;
import org.apache.edgent.samples.connectors.elm327.Elm327Cmds;

import com.google.gson.JsonObject;

/**
 * Runtime execution of ELM327 &amp; OBD-II commands.
 *
 */
public class CommandExecutor {

    public static int[] binary(byte[] reply, int offset, int length) {
        int[] binary = new int[length / 2];
        for (int i = 0; i < binary.length; i++) {
            int h = Character.digit(reply[offset++], 16);
            int l = Character.digit(reply[offset++], 16);
            binary[i] = ((h * 16) + l);
        }
        return binary;
    }

    public static void initialize(Cmd protocol, OutputStream out, InputStream in) {
        try {

            executeUntilOK(10, Elm327Cmds.INIT, out, in);
            Thread.sleep(1000);

            executeUntilOK(1, Elm327Cmds.ECHO_OFF, out, in);

            executeUntilOK(1, protocol, out, in);
            executeUntilOK(1, Elm327Cmds.SLOW_INIT, out, in);
            Thread.sleep(1000);

        } catch (Exception ioe) {
            throw new RuntimeException(ioe);
        }
    }

    private static boolean readUntilPrompt(InputStream in, ByteArrayOutputStream bytes) throws IOException {
        bytes.reset();
        for (;;) {
            int b = in.read();
            if (b == -1)
                return false;
            if (b == ' ')
                continue;
            if (b == '\r')
                continue;
            if (b == '>')
                return true;

            bytes.write(b);
        }
    }

    public static JsonObject executeUntilOK(int n, Cmd cmd, OutputStream out, InputStream in) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(16)) {
            for (int i = 0; i < n; i++) {
                cmd.writeCmd(out);
                out.flush();

                if (!readUntilPrompt(in, bytes))
                    continue;

                byte[] reply = bytes.toByteArray();
                JsonObject j = new JsonObject();
                if (cmd.result(j, reply))
                    return j;
                break;
            }
        }
        throw new IllegalStateException("Could not execute command:" + cmd);
    }

    public static JsonObject execute(Cmd cmd, OutputStream out, InputStream in) {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(16)) {
            cmd.writeCmd(out);
            out.flush();

            JsonObject result = new JsonObject();
            result.addProperty(Cmd.PID, cmd.id());
            result.addProperty(Cmd.TS, System.currentTimeMillis());

            readUntilPrompt(in, bytes);

            cmd.result(result, bytes.toByteArray());

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
