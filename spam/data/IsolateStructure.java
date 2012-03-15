package spam.dataStructures;

import spam.data.SampleMethod;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

/*
 * This class represents the overall structure of the isolates as denoted by
 * settings file or the experiment (currently it is specific to the experiment
 * at hand)
 *
 * This class will store correlations and the isolates whose comparison yields
 * the stored correlation. It is important to note that correlations are what
 * is of primary interest since this will be used to separate strong
 * connections from weak connections.
 */
public class IsolateStructure {
   //this isolate map is a mapping from days to sample methods for that day to
   //isolates for that sample method
   private Map<Integer, Map<SampleMethod, List<Isolate>>> isolateMap;
   private IsolateSimilarityMatrix similarityMatrix;

   public IsolateStructure() {
      super();

      isolateMap = new LinkedHashMap<Integer, Map<SampleMethod, List<Isolate>>>();
   }

   public boolean containsDay(int day) {
      return isolateMap.containsKey(day);
   }

   public boolean addCorrelation(Isolate isolateOne, Isolate isolateTwo, double correlation) {
      addIsolate(isolateOne);
      addIsolate(isolateTwo);


      /*
       * THIS IS TOO COMPLICATED
       */
   }

   public void addIsolate(Isolate newIsolate) {
      int day = newIsolate.getDay();
      SampleMethod sampleGroup = newIsolate.getSampleMethod();
      
      //make sure there is a mapping for the day the isolate was collected
      if (!containsDay(day)) {
         isolateMap.put(day, new HashMap<SampleMethod, List<Isolate>>());
      }

      //make sure there is a mapping for the sampling type used to collect the
      //isolate for this day
      if (!isolateMap.get(day).containsKey(sampleGroup)) {
         isolateMap.get(day).put(sampleGroup, new ArrayList<Isolate>());
      }

      List<Isolate> isolateList = isolateMap.get(day).get(sampleGroup);
      isolateList.add(newIsolate);
   }

   public List<Isolate> getIsolates(int day) {
      List<Isolate> isolateList = containsDay(day) ? isolateMap.get(day) : null;
   }

   public List<Integer> getDays() {
      return isolateMap.keySet();
   }
}
