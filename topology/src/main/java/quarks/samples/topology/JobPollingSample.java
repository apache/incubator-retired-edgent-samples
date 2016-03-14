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
package quarks.samples.topology;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import quarks.execution.Job;
import quarks.execution.JobRegistryService;
import quarks.providers.development.DevelopmentProvider;
import quarks.runtime.etiao.JobRegistry;
import quarks.topology.TStream;
import quarks.topology.Topology;

/**
 * Job monitoring by polling job state. 
 * <p>
 * Demonstrates job monitoring using the {@link JobRegistryService} service.
 * The example starts a system monitoring application then concurrently 
 * submits two jobs.  The monitoring application is using a polling source
 * to periodically scan the job registry and generates tuples containing the 
 * current state of registered jobs. Tuples are pushed to a sink which prints
 * them onto the system output.
 */
public class JobPollingSample {
    private final DevelopmentProvider dtp;
    
    public static void main(String[] args) throws Exception {
        
        JobPollingSample app = new JobPollingSample();

        // Start monitoring app
        app.monitor();

        Thread.sleep(2000);

        // Asynchronously start two jobs
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);
        executor.schedule(app.getCallable("Monitored1"), 100, TimeUnit.MILLISECONDS);
        executor.schedule(app.getCallable("Monitored2"), 3300, TimeUnit.MILLISECONDS);
    }

    JobPollingSample() throws Exception {
        this.dtp = new DevelopmentProvider();
        dtp.getServices().addService(JobRegistryService.class, new JobRegistry());
    }

    void monitored(String name) throws InterruptedException, ExecutionException {
        Topology t = dtp.newTopology(name);
        
        Random r = new Random();
        TStream<Double> d  = t.poll(() -> r.nextGaussian(), 100, TimeUnit.MILLISECONDS);
        d.sink(tuple -> System.out.print("."));

        Thread.sleep(2000);
        Future<Job> f = dtp.submit(t);
        Job job = f.get();
        Thread.sleep(5000);
        job.stateChange(Job.Action.CLOSE);
        Thread.sleep(2000);

        provider().getServices().getService(JobRegistryService.class).removeJob(job.getId());
    }
    
    /**
     * Monitoring application polls the job registry every 1 sec.
     */
    void monitor() {
        Topology t = dtp.newTopology("Monitor");

        TStream<Job.State[]> state = t.poll(() -> {
                JobRegistryService jobs = provider().getServices().getService(JobRegistryService.class);
                List<Job.State> states = new ArrayList<>();
                if (jobs != null) {
                    for (String id: jobs.getJobIds()) {
                        states.add(jobs.getJob(id).getCurrentState());
                    }
                }
                return states.toArray(new Job.State[0]);
            }, 1, TimeUnit.SECONDS);

        state.sink(states -> {
                StringBuffer sb = new StringBuffer();
                for (Job.State s : states) {
                    sb.append(s).append(',');
                }
                System.out.println(sb.toString());
            });
        
        dtp.submit(t);
    }
    
    private DevelopmentProvider provider() {
        return dtp;
    }
    
    private Callable<Integer> getCallable(String name) {
        return new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                monitored(name);
                return new Integer(0);
            }
        };
    }
}
