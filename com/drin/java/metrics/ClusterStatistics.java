package com.drin.java.metrics;

import com.drin.java.types.DataObject;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class ClusterStatistics {
   private static final boolean DEBUG = false;
   private static final String MIN_KEY = "min",
                               MAX_KEY = "max",
                               MEAN_KEY = "mean";

   public static Map<String, Double> calcStats(DataMetric<DataObject> dataMetric,
    List<DataObject> dataSet) {
      Map<String, Double> statMap = new HashMap<String, Double>();
      double min = -1, max = -1, total = 0, count = 0;

      for (int ndx_A = 0; ndx_A < dataSet.size(); ndx_A++) {
         for (int ndx_B = 0; ndx_B < dataSet.size(); ndx_B++) {
            if (!dataSet.get(ndx_A).getName().equals(dataSet.get(ndx_B).getName())) {
               dataMetric.apply(dataSet.get(ndx_A), dataSet.get(ndx_B));
               Double val = dataMetric.result();

               dataMetric.reset();

               if (DEBUG) {
                  System.out.printf("got value %s between '%s'(%s) and '%s'(%s)\n",
                   val, dataSet.get(ndx_A).getName(), dataSet.get(ndx_A).getClass().getName(),
                   dataSet.get(ndx_B).getName(), dataSet.get(ndx_B).getClass().getName());
               }
            
               if (val != null) {
                  min = min == -1 ? val.doubleValue() : Math.min(min, val.doubleValue());
                  max = Math.max(max, val.doubleValue());
                  total += val.doubleValue();
                  count++;
               }
            }
         }
      }


      if (count > 0) {
         statMap.put(MIN_KEY, min);
         statMap.put(MAX_KEY, max);
         statMap.put(MEAN_KEY, total/count);

         return statMap;
      }

      return null;
   }

   public static Double calcMean(DataMetric<DataObject> meanMetric, List<DataObject> dataSet) {
      for (int ndx_A = 0; ndx_A < dataSet.size(); ndx_A++) {
         for (int ndx_B = 0; ndx_B < dataSet.size(); ndx_B++) {
            meanMetric.apply(dataSet.get(ndx_A), dataSet.get(ndx_B));
         }
      }

      return meanMetric.result();
   }

   public static Double calcMax(DataMetric<DataObject> maxMetric, List<DataObject> dataSet) {
      for (int ndx_A = 0; ndx_A < dataSet.size(); ndx_A++) {
         for (int ndx_B = 0; ndx_B < dataSet.size(); ndx_B++) {
            maxMetric.apply(dataSet.get(ndx_A), dataSet.get(ndx_B));
         }
      }

      return maxMetric.result();
   }

   public static Double calcMin(DataMetric<DataObject> minMetric, List<DataObject> dataSet) {
      for (int ndx_A = 0; ndx_A < dataSet.size(); ndx_A++) {
         for (int ndx_B = 0; ndx_B < dataSet.size(); ndx_B++) {
            minMetric.apply(dataSet.get(ndx_A), dataSet.get(ndx_B));
         }
      }

      return minMetric.result();
   }

}
