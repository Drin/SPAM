package spam.dendogram;

import spam.dataTypes.Isolate;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;

public class TreeNode {
   private Map<String, List<Isolate>> isolateMap;
   private List<Isolate> isolateStore;

   private final String FECAL_KEY = "fecal";
   private final String IMM_KEY = "imm";
   private final String LATER_KEY = "later";
   private final String DEEP_KEY = "deep";
   private final String BEFORE_KEY = "before";

   private String mFecalSeries = "", mImmSeries = "", mLaterSeries = "",
                  mDeepSeries = "", mBeforeSeries = "";

   public TreeNode() {
      isolateMap = new HashMap<String, List<Isolate>>();
      isolateStore = new ArrayList<Isolate>();
   }

   public void addIsolate(Isolate isolate) {
      List<Isolate> isolateList = null;

      switch (isolate.getSampleMethod()) {
         case FECAL:
            if (!isolateMap.containsKey(FECAL_KEY)) {
               isolateMap.put(FECAL_KEY, new ArrayList<Isolate>());
            }

            isolateList = isolateMap.get(FECAL_KEY);
            break;
         case IMM:
            if (!isolateMap.containsKey(IMM_KEY)) {
               isolateMap.put(IMM_KEY, new ArrayList<Isolate>());
            }

            isolateList = isolateMap.get(IMM_KEY);
            break;
         case LATER:
            if (!isolateMap.containsKey(LATER_KEY)) {
               isolateMap.put(LATER_KEY, new ArrayList<Isolate>());
            }

            isolateList = isolateMap.get(LATER_KEY);
            break;
         case DEEP:
            if (!isolateMap.containsKey(DEEP_KEY)) {
               isolateMap.put(DEEP_KEY, new ArrayList<Isolate>());
            }

            isolateList = isolateMap.get(DEEP_KEY);
            break;
         case BEFORE:
            if (!isolateMap.containsKey(BEFORE_KEY)) {
               isolateMap.put(BEFORE_KEY, new ArrayList<Isolate>());
            }

            isolateList = isolateMap.get(BEFORE_KEY);
            break;
         default:
            System.err.println("unexpected sample type: " + isolate.getSampleMethod());
            System.exit(1);
      }

      if (isolateList != null) {
         isolateList.add(isolate);
         isolateStore.add(isolate);
      }
   }

   private String getSeriesCounts(String sampleType) {
      Map<Integer, Integer> sampleMap = new LinkedHashMap<Integer, Integer>();
      int numDays = 6, technicianNdx = 0, groupNdx = 1, dayNdx = 2;

      if (!isolateMap.containsKey(sampleType)) {
         isolateMap.put(sampleType, new ArrayList<Isolate>());
      }

      for (Isolate sample : isolateMap.get(sampleType)) {
         String sampleName = sample.getName();
         //if isolate name is 'f14-1' then extract 14 as the day
         int day = Integer.parseInt(sampleName.substring(dayNdx, sampleName.indexOf("-")));
         //if isolate name is 'f14-1' then extract 1 as the isolateNum
         int isolateNum = Integer.parseInt(sampleName.substring(sampleName.indexOf("-") + 1, sampleName.length()));
         int isolateCount = 0;
         
         if (!sampleMap.containsKey(day)) {
            sampleMap.put(day, 1);
         }
         else {
            sampleMap.put(day, sampleMap.get(day) + 1);
         }
      }

      //will display Day:, 1, 2, 3, ... for csv formatted temporal diagram
      String sampleSeries = "";

      for (int day = 1; day <= numDays; day++) {
         sampleSeries += "," + (sampleMap.containsKey(day) ? sampleMap.get(day) : 0);
      }

      return sampleSeries.substring(1);
   }

   public String getFecalSeries() {
      return getSeriesCounts(FECAL_KEY);
   }

   public String getImmSeries() {
      return getSeriesCounts(IMM_KEY);
   }

   public String getLaterSeries() {
      return getSeriesCounts(LATER_KEY);
   }

   public String getDeepSeries() {
      return getSeriesCounts(DEEP_KEY);
   }

   public String getBeforeSeries() {
      return getSeriesCounts(BEFORE_KEY);
   }

   public List<Isolate> getIsolateList() {
      return isolateStore;
   }
}
