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

import org.apache.edgent.function.Supplier;

/**
 * A Supplier&lt;String&gt; for creating sample messages to publish.
 */
public class MsgSupplier implements Supplier<String> {
    private static final long serialVersionUID = 1L;
    private final int maxCnt;
    private int cnt;
    private boolean done;
    
    public MsgSupplier(int maxCnt) {
        this.maxCnt = maxCnt;
    }

    @Override
    public synchronized String get() {
        ++cnt;
        if (maxCnt >= 0 && cnt >= maxCnt) {
            if (!done) {
                done = true;
                System.out.println("poll: no more messages to generate.");
            }
            return null;
        }
        String msg = String.format("Message-%d from %s", cnt, Util.simpleTS());
        System.out.println("poll generated msg to publish: " + msg);
        return msg;
    }
}
