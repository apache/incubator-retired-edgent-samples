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
import java.util.List;
import java.util.Properties;

import org.apache.edgent.connectors.jdbc.JdbcStreams;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * A simple JDBC connector sample demonstrating streaming read access
 * of a dbms table and creating stream tuples from the results.
 */
public class SimpleReaderApp {
    private final Properties props;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to jdbc.properties file");
        SimpleReaderApp reader = new SimpleReaderApp(args[0]);
        reader.run();
    }

    /**
     * @param jdbcPropsPath pathname to properties file
     */
    SimpleReaderApp(String jdbcPropsPath) throws Exception {
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
        
        // Create a sample stream of tuples containing a person id
        List<PersonId> personIdList = PersonData.toPersonIds(PersonData.loadPersonData(props));
        personIdList.add(new PersonId(99999));
        TStream<PersonId> personIds = t.collection(personIdList);
        
        // For each tuple on the stream, read info from the db table
        // using the "id", and create a Person tuple on the result stream.
        TStream<Person> persons = myDb.executeStatement(personIds,
                () -> "SELECT id, firstname, lastname FROM persons WHERE id = ?",
                (personId,stmt) -> stmt.setInt(1, personId.id),
                (personId,rSet,exc,resultStream) -> {
                        if (exc != null) {
                            // some failure processing this tuple. an error was logged.
                            System.err.println("Unable to process id="+personId+": "+exc);
                            return;
                        }
                        if (rSet.next()) {
                            resultStream.accept(
                                    new Person(rSet.getInt("id"),
                                            rSet.getString("firstname"),
                                            rSet.getString("lastname")));
                        }
                        else {
                            System.err.println("Unknown person id="+personId.id);
                        }
                    }
                );
        
        // print out Person tuples as they are retrieved 
        persons.sink(person -> System.out.println("retrieved person: "+person));
        
        // run the application / topology
        tp.submit(t);
    }
}
