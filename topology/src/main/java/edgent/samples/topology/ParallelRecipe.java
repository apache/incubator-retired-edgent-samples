package edgent.samples.topology;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import edgent.console.server.HttpServer;
import edgent.function.BiFunction;
import edgent.function.Functions;
import edgent.function.ToIntFunction;
import edgent.providers.development.DevelopmentProvider;
import edgent.providers.direct.DirectProvider;
import edgent.samples.utils.sensor.SimpleSimulatedSensor;
import edgent.topology.TStream;
import edgent.topology.Topology;
import edgent.topology.plumbing.PlumbingStreams;

/**
 * A recipe for parallel analytics.
 */
public class ParallelRecipe {

    /**
     * Process several tuples in parallel in a replicated pipeline.
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {

        DirectProvider dp = new DevelopmentProvider();
        System.out.println("development console url: "
                + dp.getServices().getService(HttpServer.class).getConsoleUrl());

        Topology top = dp.newTopology("ParallelRecipe");

        // The number of parallel processing channels to generate
        int width = 5;
        
        // Define the splitter
        ToIntFunction<Double> splitter = PlumbingStreams.roundRobinSplitter(width);
        
        // Generate a polled simulated sensor stream
        SimpleSimulatedSensor sensor = new SimpleSimulatedSensor();
        TStream<Double> readings = top.poll(sensor, 10, TimeUnit.MILLISECONDS)
                                      .tag("readings");
        
        // Build the parallel analytic pipelines flow
        TStream<String> results = 
            PlumbingStreams.parallel(readings, width, splitter, pipeline())
            .tag("results");
        
        // Print out the results.
        results.sink(tuple -> System.out.println(new Date().toString() + "   " + tuple));

        System.out.println("Notice that "+width+" results are generated every second - one from each parallel channel."
            + "\nOnly one result would be generated each second if performed serially.");
        dp.submit(top);
    }
    
    /** Function to create analytic pipeline and add it to a stream */
    private static BiFunction<TStream<Double>,Integer,TStream<String>> pipeline() {
        // a simple 3 stage pipeline simulating some amount of work by sleeping
        return (stream, channel) -> 
          { 
            String tagPrefix = "pipeline-ch"+channel;
            return stream.map(tuple -> {
                sleep(1000, TimeUnit.MILLISECONDS);
                return "This is the "+tagPrefix+" result for tuple "+tuple;
              }).tag(tagPrefix+".stage1")
              .map(Functions.identity()).tag(tagPrefix+".stage2")
              .map(Functions.identity()).tag(tagPrefix+".stage3");
          };
    }

    private static void sleep(long period, TimeUnit unit) throws RuntimeException {
        try {
            Thread.sleep(unit.toMillis(period));
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted", e);
        }
    }

}

