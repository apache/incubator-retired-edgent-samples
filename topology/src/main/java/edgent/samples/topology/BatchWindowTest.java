package edgent.samples.topology;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonObject;

import edgent.function.Function;
import edgent.providers.direct.DirectProvider;
import edgent.topology.TStream;
import edgent.topology.TWindow;
import edgent.topology.Topology;

public class BatchWindowTest {
  
  boolean useTimeBasedWindow = true;
  boolean useSlidingWindow = false;//true;

  public static void main(String[] args) {
    new BatchWindowTest().run();
  }
  
  private void run()  {
    DirectProvider dp = new DirectProvider();
    Topology top = dp.newTopology();
    
    System.out.println("Config: "
        + " useTimeBasedWindow=" + useTimeBasedWindow
        + " useSlidingWindow=" + useSlidingWindow
        );

    AtomicInteger cntA = new AtomicInteger();
    AtomicInteger cntB = new AtomicInteger();
    AtomicInteger aggCnt = new AtomicInteger();
    
    Function<JsonObject,String> keyFn = jo -> jo.get("key").getAsString();
    
    TStream<JsonObject> sA = top.poll(() -> {
        JsonObject jo = new JsonObject();
        jo.addProperty("key", "A");
        jo.addProperty("cnt", cntA.incrementAndGet());
        return jo;
      }, 1, TimeUnit.SECONDS);
    
    // sB is half the rate of sA
    TStream<JsonObject> sB = top.poll(() -> {
        JsonObject jo = new JsonObject();
        jo.addProperty("key", "B");
        jo.addProperty("cnt", cntB.incrementAndGet());
        return jo;
      }, 2, TimeUnit.SECONDS);
    
    TStream<JsonObject> s = sA.union(sB);

    // record (and report below) what's going into the window
    Map<String,Integer> byKeyCntMap = new HashMap<>();
    s.peek(jo -> {
      String key = keyFn.apply(jo);
      Integer i = byKeyCntMap.get(key);
      if (i == null)
        i = 0;
      i++;
      byKeyCntMap.put(key, i);
    });
    
    TWindow<JsonObject,String> w = null;
    if (useTimeBasedWindow)
      w = s.last(5, TimeUnit.SECONDS, keyFn);
    else
      w = s.last(5, keyFn);
    
    TStream<String> result = null;
    if (useSlidingWindow) {
      result = w.aggregate(
        (list,key) -> {
          return "sliding agg: aggCnt="+aggCnt.incrementAndGet()
              +" list="+list
              +" cntA="+cntA.get()
              +" cntB="+cntB.get()
              +" byKeyCnts="+byKeyCntMap;
        });
    }
    else {
      result = w.batch(
          (list,key) -> {
            return "batch agg: aggCnt="+aggCnt.incrementAndGet()
                +" list="+list
                +" cntA="+cntA.get()
                +" cntB="+cntB.get()
                +" byKeyCnts="+byKeyCntMap;
          });
    }
    
    result.print();
    
    dp.submit(top);
  }

}
