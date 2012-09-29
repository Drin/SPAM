package com.drin.java.metrics;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.metrics.ITSRegionMetric;

import java.util.Map;

public class IsolateComparator implements DataComparator<Isolate, IsolateMetric> {
   private static final boolean DEBUG = false;

   @Override
   public Double compare(IsolateMetric isoMetric, Isolate elem_A, Isolate elem_B) {
      Double comparison = null;

      if (elem_A.getRegions().size() == elem_B.getRegions().size()) {
         for (ITSRegion region_A : elem_A.getRegions()) {
            for (ITSRegion region_B : elem_B.getRegions()) {
               if (region_A.getName().equals(region_B.getName())) {
                  isoMetric.apply(region_A, region_B);
               }
            }
         }
      }

      if (DEBUG) { System.out.printf("Isolate Comparator:\n\tisolate comparison: %.04f\n", isoMetric.result()); }

      comparison = isoMetric.result();
      isoMetric.reset();

      return comparison;
   }

   @Override
   public boolean isSimilar(IsolateMetric metric, Isolate data_A, Isolate data_B) {
      throw new UnsupportedOperationException();
      /*
      Double comparison = compare(metric, data_A, data_B);

      return comparison != null && comparison.compareTo(metric.getAlphaThreshold()) >= 0;
      */
   }
}
