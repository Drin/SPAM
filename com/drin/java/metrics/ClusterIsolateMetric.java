package com.drin.java.metrics;

import com.drin.java.types.Isolate;
import com.drin.java.types.ITSRegion;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.IsolateMetric;
import com.drin.java.metrics.IsolateComparator;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public class ClusterIsolateMetric implements DataMetric<Isolate> {
   private static final boolean DEBUG = false;
   protected IsolateComparator mRegionComp;
   protected IsolateMetric mIsoMetric;
   protected Double mResult;

   public ClusterIsolateMetric(IsolateComparator isoComp, IsolateMetric isoMetric) {
      mRegionComp = isoComp;
      mIsoMetric = isoMetric;
      mResult = null;
   }

   @Override
   public void apply(Isolate elem_A, Isolate elem_B) {
      mResult = mRegionComp.compare(mIsoMetric, elem_A, elem_B);

      if (DEBUG) {
         System.out.printf("Cluster Isolate Metric:\n\t" +
          "result: %.04f\n", mResult);
      }
   }

   @Override
   public void reset() {
      mResult = null;
   }

   @Override
   public Double result() {
      if (mResult == null) {Logger.debug("Cluster metric has no result to report!"); }
      return mResult;
   }
}
