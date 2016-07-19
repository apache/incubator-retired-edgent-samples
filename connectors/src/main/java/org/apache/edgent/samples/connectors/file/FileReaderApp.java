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
package org.apache.edgent.samples.connectors.file;

import java.io.File;

import org.apache.edgent.connectors.file.FileStreams;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * Watch a directory for files and convert their contents into a stream.
 */
public class FileReaderApp {
    private final String directory;
    private static final String baseLeafname = "FileSample";

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to an existing directory");
        FileReaderApp reader = new FileReaderApp(args[0]);
        reader.run();
    }
   
    /**
     * 
     * @param directory an existing directory to watch for file
     */
    public FileReaderApp(String directory) {
        File dir = new File(directory);
        if (!dir.exists())
            throw new IllegalArgumentException("directory doesn't exist");
        this.directory = directory;
    }
    
    public void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application / topology
        
        Topology t = tp.newTopology("FileSample consumer");

        // watch for files
        TStream<String> pathnames = FileStreams.directoryWatcher(t, () -> directory);
        
        // create a stream containing the files' contents.
        // use a preFn to include a separator in the results.
        // use a postFn to delete the file once its been processed.
        TStream<String> contents = FileStreams.textFileReader(pathnames,
                tuple -> "<PRE-FUNCTION> "+tuple, 
                (tuple,exception) -> {
                    // exercise a little caution in case the user pointed
                    // us at a directory with other things in it
                    if (tuple.contains("/"+baseLeafname+"_")) { 
                        new File(tuple).delete();
                    }
                    return null;
                });
        
        // print out what's being read
        contents.print();
        
        // run the application / topology
        System.out.println("starting the reader watching directory " + directory);
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
