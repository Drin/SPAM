package com.drin.java.metrics;

import com.drin.java.metrics.ITSRegionComparator;
import com.drin.java.metrics.ClusterMetric;
import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Isolate;
import com.drin.java.util.Logger;

import java.util.Map;

public class IsolateAverageMetric extends IsolateMetric {
   private static final boolean DEBUG = false;
   private int mLinkCount;

   public IsolateAverageMetric(ITSRegionComparator regionComp, ITSRegionMetric regionMetric) {
      super(regionComp, regionMetric);
      mLinkCount = 0;
   }

   public IsolateAverageMetric(ITSRegionComparator regionComp,
    ITSRegionMetric regionMetric_A, ITSRegionMetric regionMetric_B) {
      super(regionComp, regionMetric_A, regionMetric_B);
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      double regionValue = 0;
      int regionCount = 0;

      for (ITSRegionMetric metric : mRegionMetrics) {
         Double comparison = mRegionComp.compare(metric, elem_A, elem_B);

         if (DEBUG) { if (comparison == null) { System.out.printf("Isolate Average metric:\n\tregion comparison is null!\n"); } }
      
         if (comparison != null) {
            if (DEBUG) { System.out.printf("Isolate Average Metric:\n\tregion comparison is %.04f\n", comparison.doubleValue()); }
            regionValue = regionValue + comparison.doubleValue();
            regionCount++;
         }
      }

      if (regionCount > 0) {
         if (DEBUG) {
            System.out.printf("Isolate Average Metric:\n\tregion comparison is %.04f\n\tresult is %.04f\n",
             regionValue, regionValue/regionCount);
         }

         if (mResult != null) { mResult = new Double(mResult.doubleValue() + (regionValue / regionCount)); }
         else { mResult = new Double(regionValue / regionCount); }

         mLinkCount++;
      }
   }

   public void reset() {
      super.reset();
      mLinkCount = 0;
   }

   @Override
   public Double result() {
      if (mResult != null) {
         if (DEBUG) {
            System.out.printf("Isolate Average Metric:\n\tResulting comparison is %.04f(%.04f/%d)\n",
             mResult.doubleValue()/mLinkCount, mResult.doubleValue(), mLinkCount);
         }

         return new Double(mResult.doubleValue()/mLinkCount);
      }

      else {
         Logger.debug("Cluster metric has no result to report!");
         return null;
      }
   }
}
