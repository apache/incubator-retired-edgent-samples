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
package org.apache.edgent.samples.topology;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.edgent.execution.Job;
import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * Using the Job API to get/set a job's state.
 */
public class JobExecution {
    public final static long JOB_LIFE_MILLIS = 10000;
    public final static long WAIT_AFTER_CLOSE = 2000;

    public static void main(String[] args) throws ExecutionException {

        DirectProvider tp = new DirectProvider();

        Topology t = tp.newTopology("JobExecution");

        // Source
        Random r = new Random();
        TStream<Double> gaussian = t.poll(() -> r.nextGaussian(), 1, TimeUnit.SECONDS);

        // Peek
        gaussian = gaussian.peek(g -> System.out.println("R:" + g));
  
        // Transform to strings
        TStream<String> gsPeriodic = gaussian.map(g -> "G:" + g + ":");
        gsPeriodic.print();
  
        // Submit job and poll its status for a while
        Future<Job> futureJob = tp.submit(t);
        Reporter reporter = new Reporter();
        try {
            Job job = futureJob.get();
            reporter.start(job);

            // Wait for the job to complete
            try {
                job.complete(JOB_LIFE_MILLIS, TimeUnit.MILLISECONDS);
                System.out.println("The job completed successfully");
            } catch (ExecutionException e) {
                System.out.println("The job aborted by throwing exception: " + e);
            }
            catch (InterruptedException e) {
                System.out.println("Interrupted while waiting for the job to complete");
            }
            catch (TimeoutException e) {
                System.out.println("Timed out while waiting for the job to complete");
            }
            finally {
                System.out.println("Closing the job...");
                job.stateChange(Job.Action.CLOSE);
            }
            System.out.println("Sleep after job close for " + WAIT_AFTER_CLOSE + " ms");
            Thread.sleep(WAIT_AFTER_CLOSE);
        }
        catch (InterruptedException e) {
            System.err.println("Interrupted!");
        }
        finally {
            reporter.stop();
        }
    }

    static class Reporter implements Runnable {
        private volatile Job job;
        private volatile Thread runner;
        
        @Override
        public void run() {
            try {
                while (true) {
                    if (job != null)
                        System.out.println("Job state is: current=" + job.getCurrentState() + 
                                " next=" + job.getNextState());
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                System.out.println("Reporter interrupted");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        
        void start(Job job) {
            this.job = job;
            runner = Executors.defaultThreadFactory().newThread(this);
            runner.setName("Reporter");
            runner.setDaemon(false);
            runner.start();
        }
        
        void stop() {
            runner.interrupt();
        }
    }
}
