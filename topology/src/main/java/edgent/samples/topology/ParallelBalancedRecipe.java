package edgent.samples.topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;

import com.google.gson.Gson;

import edgent.console.server.HttpServer;
import edgent.execution.Job;
import edgent.execution.services.ControlService;
import edgent.function.BiFunction;
import edgent.function.Functions;
import edgent.providers.development.DevelopmentProvider;
import edgent.providers.direct.DirectProvider;
import edgent.samples.utils.sensor.SimpleSimulatedSensor;
import edgent.streamscope.StreamScope;
import edgent.streamscope.StreamScopeRegistry;
import edgent.streamscope.StreamScope.Sample;
import edgent.streamscope.mbeans.StreamScopeMXBean;
import edgent.streamscope.mbeans.StreamScopeRegistryMXBean;
import edgent.topology.TStream;
import edgent.topology.Topology;
import edgent.topology.plumbing.PlumbingStreams;

/**
 * A recipe for load balanced parallel analytics.
 */
public class ParallelBalancedRecipe {

    /**
     * Process several tuples in parallel in a replicated pipeline.
     * @param args command arguments
     * @throws Exception on failure
     */
    public static void main(String[] args) throws Exception {

        DirectProvider dp = new DevelopmentProvider();
        System.out.println("development console url: "
                + dp.getServices().getService(HttpServer.class).getConsoleUrl());

        Topology top = dp.newTopology("ParallelBalancedRecipe");

        // The number of parallel processing channels to generate
        int width = 2;
        
        // Generate a polled simulated sensor stream
        SimpleSimulatedSensor sensor = new SimpleSimulatedSensor();
        TStream<Double> readings = top.poll(sensor, 100, TimeUnit.MILLISECONDS)
                                      .alias("readings-source").tag("readings");
        
        // Build the parallel analytic pipelines flow
        TStream<String> results = 
            PlumbingStreams.parallelBalanced(readings, width, pipeline())
            .tag("results");
        
        // Print out the results.
        results.sink(tuple -> System.out.println(new Date().toString() + "   " + tuple));

        System.out.println("Notice that ch-1 processes three times as many results as ch-0");
        Future<Job> job = dp.submit(top);
        
        // need to get the job to ensure it's initialized/running before
        // we try to get things from the registry
        job.get();
        dumpStreamScopeRegistry(top);
        String streamId = pickOneStreamScopeStreamId(top);
        enableStreamScope(top, streamId);
        while(true) {
          Thread.sleep(5*1000);
//          dumpStreamScope(top, streamId);
          dumpStreamScopeBean(top, streamId);
        }
    }
    
    private static class StreamId {
      private final String streamId;
      private String jobId;
      private String opletId;
      private int oport;
      
      // streamId from StreamScopeRegistry.mkStreamId()
      StreamId(String streamId) {
        this.streamId = streamId;
        parse();
      }
      
      private void parse() {
        try (Scanner s = new Scanner(streamId)) {
          // pattern from StreamScopeRegistry.mkStreamId()
          s.findInLine("j\\[(\\w+)\\].op\\[(\\w+)\\].o\\[(\\w+)\\]");
          MatchResult result = s.match();
          jobId = result.group(1);
          opletId = result.group(2);
          oport = Integer.valueOf(result.group(3));
        }
      }
      
      String jobId() { return jobId; }
      String opletId() { return opletId; }
      int oport() { return oport; }
    }
    
    private static void dumpStreamScopeRegistry(Topology top) {
      StreamScopeRegistry rgy = (StreamScopeRegistry)
          top.getRuntimeServiceSupplier().get()
            .getService(StreamScopeRegistry.class);
      if (rgy == null) {
        System.out.println("No StreamScopeRegistry");
        return;
      }
      Map<StreamScope<?>,List<String>> streamScopes = rgy.getStreamScopes();
      System.out.println("streamScopes: "+streamScopes);
    }
    
    private static String pickOneStreamScopeStreamId(Topology top) {
      StreamScopeRegistry rgy = (StreamScopeRegistry)
          top.getRuntimeServiceSupplier().get()
            .getService(StreamScopeRegistry.class);
      if (rgy == null) {
        System.out.println("No StreamScopeRegistry");
        return null;
      }
      List<String> names = new ArrayList<>(rgy.getNames());
      Collections.sort(names);
      for (String name : names) {
        if (name.startsWith("id."))
          return name.substring("id.".length());
      }
      return null;
    }
    
    private static void dumpStreamScope(Topology top, String streamId) {
      StreamScopeRegistry rgy = (StreamScopeRegistry)
          top.getRuntimeServiceSupplier().get()
            .getService(StreamScopeRegistry.class);
      if (rgy == null) {
        System.out.println("No StreamScopeRegistry");
        return;
      }
      String name = StreamScopeRegistry.nameForStreamId(streamId);
      StreamScope<?> streamScope = rgy.lookup(name);
      System.out.println("streamScope-"+streamId+": "+streamScope);
      List<?> samples = streamScope.getSamples();
      for (Object o : samples) {
        Sample<?> sample = (Sample<?>) o;
        System.out.println("sample: "+sample);
      }
      
    }
    
    private static void dumpStreamScopeBean(Topology top, String streamId) {
      ControlService cs =
          top.getRuntimeServiceSupplier().get()
            .getService(ControlService.class);
      if (cs == null) {
        System.out.println("No ControlService");
        return;
      }
      StreamScopeRegistryMXBean rgy = cs.getControl(
          StreamScopeRegistryMXBean.TYPE, StreamScopeRegistryMXBean.TYPE,
          StreamScopeRegistryMXBean.class);
      if (rgy == null) {
        System.out.println("No StreamScopeRegistry");
        return;
      }
      StreamId id = new StreamId(streamId);
      StreamScopeMXBean streamScope = rgy.lookup(id.jobId(), id.opletId(), id.oport());
      String json = streamScope.getSamples();
      
      System.out.println("samples: "+json);
      Gson gson = new Gson();
//      JsonObject[] ja = gson.fromJson(json, JsonObject[].class);
//      for (JsonObject jo : ja) {
//        System.out.println("sample: "+jo);
//      }
      Sample<?>[] sa = gson.fromJson(json, Sample[].class);
      for (Sample<?> s : sa) {
        System.out.println("sample: "+s);
      }
      
    }
    
    private static void enableStreamScope(Topology top, String streamId) {
      StreamScopeRegistry rgy = (StreamScopeRegistry)
          top.getRuntimeServiceSupplier().get()
            .getService(StreamScopeRegistry.class);
      if (rgy == null) {
        System.out.println("No StreamScopeRegistry");
        return;
      }
      String name = StreamScopeRegistry.nameForStreamId(streamId);
      StreamScope<?> streamScope = rgy.lookup(name);
      streamScope.setEnabled(true);
      System.out.println("streamScope-"+streamId+": "+streamScope);
    }
    
    /** Function to create analytic pipeline and add it to a stream */
    private static BiFunction<TStream<Double>,Integer,TStream<String>> pipeline() {
        // a simple 3 stage pipeline simulating some amount of work by sleeping
        return (stream, channel) -> 
          { 
            String tagPrefix = "pipeline-ch"+channel;
            return stream.map(tuple -> {
                // make odd number channels 3 times faster than even channels.
                long sleepMsec = new long[]{2000, 660}[channel % 2];
                sleep(sleepMsec, TimeUnit.MILLISECONDS);
                return "This is the "+tagPrefix+" ("+sleepMsec+"Msec) result for tuple "+tuple;
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

