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
package org.apache.edgent.samples.connectors.jdbc;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.edgent.connectors.jdbc.JdbcStreams;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * A simple JDBC connector sample demonstrating streaming write access
 * of a dbms to add stream tuples to a table.
 */
public class SimpleWriterApp {
    private final Properties props;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to jdbc.properties file");
        SimpleWriterApp writer = new SimpleWriterApp(args[0]);
        DbUtils.initDb(DbUtils.getDataSource(writer.props));
        writer.run();
    }

    /**
     * @param jdbcPropsPath pathname to properties file
     */
    SimpleWriterApp(String jdbcPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(jdbcPropsPath).toPath()));
    }
    
    /**
     * Create a topology for the writer application and run it.
     */
    private void run() throws Exception {
        DirectProvider tp = new DirectProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("jdbcSampleWriter");

        // Create the JDBC connector
        JdbcStreams myDb = new JdbcStreams(t,
                () -> DbUtils.getDataSource(props),
                dataSource -> dataSource.getConnection());
        
        // Create a sample stream of Person tuples
        TStream<Person> persons = t.collection(PersonData.loadPersonData(props));
        
        // Write stream tuples to a table.
        myDb.executeStatement(persons,
                () -> "INSERT INTO persons VALUES(?,?,?)",
                (person,stmt) -> {
                    System.out.println("Inserting into persons table: person "+person);
                    stmt.setInt(1, person.id);
                    stmt.setString(2, person.firstName);
                    stmt.setString(3, person.lastName);
                    }
                );
        
        // run the application / topology
        tp.submit(t);
    }
}
