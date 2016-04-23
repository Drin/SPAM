package com.drin.metrics;

import com.drin.clustering.Clusterable;
import com.drin.clustering.Cluster;

import com.drin.metrics.DataMetric;

import com.drin.util.Logger;

public class ClusterAverageMetric extends DataMetric<Cluster> {
   private int mLinkCount;

   public ClusterAverageMetric() {
      this.reset();
   }

   @Override
   public void apply(Cluster data_A, Cluster data_B) {
      for (Clusterable<?> elem_A : data_A.getElements()) {
         for (Clusterable<?> elem_B : data_B.getElements()) {
            mResult += elem_A.compareTo(elem_B);
            mLinkCount++;
         }
      }
   }

   @Override
   public void reset() {
      mResult = 0;
      mLinkCount = 0;
   }

   @Override
   public double result() {
      double result = mResult;

      if (mLinkCount <= 0) { setError(-1); }
      else { result /= mLinkCount; }

      this.reset();
      return result;
   }
}
