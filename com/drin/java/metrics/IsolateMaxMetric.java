package com.drin.java.metrics;

import com.drin.java.metrics.ITSRegionComparator;
import com.drin.java.metrics.IsolateMetric;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;
import com.drin.java.util.Logger;

import java.util.Map;

public class IsolateMaxMetric extends IsolateMetric {

   public IsolateMaxMetric(ITSRegionComparator regionComp, ITSRegionMetric regionMetric) {
      super(regionComp, regionMetric);
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      //TODO
      for (ITSRegionMetric metric : mRegionMetrics) {
         Double comparison = mRegionComp.compare(metric, elem_A, elem_B);
      
         if (comparison != null && mResult != null) {
            mResult = new Double(Math.max(mResult.doubleValue(), comparison.doubleValue()));
         }
         else if (comparison != null) {
            mResult = comparison;
         }
      }
   }
}
