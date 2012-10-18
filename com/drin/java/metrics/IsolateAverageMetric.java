package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;

import com.drin.java.metrics.ITSRegionComparator;
import com.drin.java.metrics.ITSRegionMetric;

import com.drin.java.util.Logger;

import java.util.Map;

public class IsolateAverageMetric extends IsolateMetric {

   public IsolateAverageMetric(ITSRegionComparator regionComp,
                               ITSRegionMetric regionMetric) {
      super(regionComp, regionMetric);
   }

   public IsolateAverageMetric(ITSRegionComparator regionComp,
                               ITSRegionMetric regionMetric_A,
                               ITSRegionMetric regionMetric_B) {
      super(regionComp, regionMetric_A, regionMetric_B);
   }

   @Override
   public void apply(Isolate elem_A, Isolate elem_B) {
      double total = 0;
      int regionCount = 0;

      Map<String, ITSRegion> regionMap_A = elem_A.getRegions();
      Map<String, ITSRegion> regionMap_B = elem_B.getRegions();

      for (ITSRegionMetric metric : mRegionMetrics) {
         ITSRegion region_A = regionMap_A.get(metric.getAppliedRegion().getName());
         ITSRegion region_B = regionMap_B.get(metric.getAppliedRegion().getName());

         double comparison = mRegionComp.compare(metric, region_A, region_B);

         Logger.debug(String.format("Isolate Average Metric:\n\tregion " +
                                    "comparison is %.04f for region %s",
                                    comparison, region_A.getName()));

         total += comparison;
         regionCount++;

         if (regionCount > 2) { setError(1); }
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
