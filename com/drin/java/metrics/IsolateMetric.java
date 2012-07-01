package com.drin.java.metrics;

import com.drin.java.types.ITSRegion;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ITSRegionComparator;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public abstract class IsolateMetric implements DataMetric<ITSRegion> {
   protected ITSRegionComparator mRegionComp;
   protected Set<ITSRegionMetric> mRegionMetrics;
   protected Double mResult;

   protected IsolateMetric() {
      mRegionMetrics = new HashSet<ITSRegionMetric>();
      mResult = null;
   }

   public IsolateMetric(ITSRegionComparator regionComp, ITSRegionMetric regionMetric) {
      this();
      mRegionComp = regionComp;
      mRegionMetrics.add(regionMetric);
   }

   public IsolateMetric(ITSRegionComparator regionComp, ITSRegionMetric regionMetric,
    ITSRegionMetric regionMetric2) {
      this(regionComp, regionMetric);

      mRegionMetrics.add(regionMetric2);
   }

   @Override
   public abstract void apply(ITSRegion elem_A, ITSRegion elem_B);

   public void reset() {
      mResult = null;
   }

   @Override
   public Double result() {
      if (mResult == null) {Logger.debug("Cluster metric has no result to report!"); }
      return mResult;
   }
}
