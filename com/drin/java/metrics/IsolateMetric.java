package com.drin.java.metrics;

import com.drin.java.biology.Isolate;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ITSRegionComparator;

import com.drin.java.util.Logger;

import java.util.Set;
import java.util.HashSet;

public abstract class IsolateMetric implements DataMetric<Isolate> {
   protected double mResult;
   protected int mErrCode;
   protected ITSRegionComparator mRegionComp;
   protected Set<ITSRegionMetric> mRegionMetrics;

   protected IsolateMetric() {
      mRegionMetrics = new HashSet<ITSRegionMetric>();
      this.reset();
   }

   public IsolateMetric(ITSRegionComparator regionComp,
                        ITSRegionMetric metric) {
      this();
      mRegionComp = regionComp;
      mRegionMetrics.add(metric);
   }

   public IsolateMetric(ITSRegionComparator regionComp,
                        ITSRegionMetric metric1,
                        ITSRegionMetric metric2) {
      this(regionComp, metric1);
      mRegionMetrics.add(metric2);
   }

   @Override
   public abstract void apply(Isolate elem_A, Isolate elem_B);

   @Override
   public void reset() {
      mResult = 0;
      mErrCode = 0;

      Logger.debug("Resetting IsolateMetric");
   }

   @Override
   public double result() {
      double result = mResult;

      Logger.error(mErrCode, "IsolateMetric(Line 48): " +
                             "Error computing Isolate Metric");

      Logger.debug(String.format("Isolate Average Metric:\n\tResulting " +
                                 "comparison is %.04f(%.04f/%d)", mResult));

      this.reset();
      return mResult;
   }

   @Override
   public void setError(int errCode) { mErrCode = errCode; }

   @Override
   public int getError() { return mErrCode; }
}
