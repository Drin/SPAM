package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Iterator;

public class IsolateAverageMetric extends DataMetric<Isolate> {
   public IsolateAverageMetric() {
      super();
   }

   @Override
   public void apply(Isolate elem_A, Isolate elem_B) {
      double total = 0;
      int regionCount = 0;

      Iterator<ITSRegion> itr_A = elem_A.getData().iterator();

      while (itr_A.hasNext()) {
         ITSRegion region_A = itr_A.next();
         Iterator<ITSRegion> itr_B = elem_B.getData().iterator();

         while (itr_B.hasNext()) {
            ITSRegion region_B = itr_B.next();

            if (region_A.getName().equals(region_B.getName())) {
               double comparison = region_A.compareTo(region_B);

               Logger.debug(String.format("Isolate Average Metric:\n\tregion " +
                                          "comparison is %.04f for region %s",
                                          comparison, region_A.getName()));

               total += comparison;
               regionCount++;

               if (regionCount > 2) { setError(1); }

               break;
            }

         }
      }

      if (regionCount > 0) {
         Logger.debug(String.format("Isolate Average Metric:\n\tregion " +
                                    "comparison is %.04f\n\tresult is " +
                                    "%.04f", total, total/regionCount));

         if (mResult != 0) { setError(-1); }
         mResult = total / regionCount;
      }

      else { setError(-1); }
   }
}
