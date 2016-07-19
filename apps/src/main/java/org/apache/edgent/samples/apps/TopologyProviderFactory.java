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

import java.util.Properties;

import org.apache.edgent.providers.direct.DirectProvider;

/**
 * A configuration driven factory for an Edgent topology provider.
 */
public class TopologyProviderFactory {
    private final Properties props;
    
    /**
     * Construct a factory
     * @param props configuration information.
     */
    public TopologyProviderFactory(Properties props) {
        this.props = props;
    }
    
    /**
     * Get a new topology provider.
     * <p>
     * The default provider is {@code org.apache.edgent.providers.direct.DirectProvider}.
     * <p>
     * The {@code topology.provider} configuration property can specify
     * an alternative.
     * 
     * @return the provider
     * @throws Exception if the provider couldn't be created
     */
    public DirectProvider newProvider() throws Exception {
        String name = props.getProperty("topology.provider", "org.apache.edgent.providers.direct.DirectProvider");
        Class<?> clazz = null;
        try {
            clazz = Class.forName(name);
        }
        catch (ClassNotFoundException e) {
            String msg = "Class not found: "+e.getLocalizedMessage();
            System.err.println(msg);
            throw new IllegalStateException(msg);
        }
        return (DirectProvider) clazz.newInstance();
    }
}
