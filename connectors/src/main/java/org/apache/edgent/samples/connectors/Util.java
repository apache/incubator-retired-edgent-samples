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
package org.apache.edgent.samples.connectors;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.execution.Job;

/**
 * Utilities for connector samples.
 */
public class Util {

    /**
     * Generate a simple timestamp with the form {@code HH:mm:ss.SSS}
     * @return the timestamp
     */
    public static String simpleTS() {
        return new SimpleDateFormat("HH:mm:ss.SSS").format(new Date());
    }

    
    /**
     * Wait for the job to reach the specified state.
     * <p>
     * A placeholder till GraphJob directly supports awaitState()?
     * @param job the job
     * @param state the state to wait for
     * @param timeout specify -1 to wait forever (until interrupted)
     * @param unit may be null if timeout is -1
     * @return true if the state was reached, false otherwise: the time limit
     * was reached of the thread was interrupted.
     */
    public static boolean awaitState(Job job, Job.State state, long timeout, TimeUnit unit) {
        long endWait = -1;
        if (timeout != -1) {
            endWait = System.currentTimeMillis()
                        + unit.toMillis(timeout);
        }
        while (true) {
            Job.State curState = job.getCurrentState();
            if (curState == state)
                return true;
            if (endWait != -1) {
                long now = System.currentTimeMillis();
                if (now >= endWait)
                    return false;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return false;
            }
        }
    }

}
