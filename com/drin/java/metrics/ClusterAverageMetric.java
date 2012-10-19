package com.drin.java.metrics;

import com.drin.java.clustering.BaseClusterable;
import com.drin.java.clustering.Cluster;

import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.ClusterMetric;

import com.drin.java.util.Logger;

public class ClusterAverageMetric<E extends BaseClusterable> extends ClusterMetric<E> {
   private int mLinkCount;

   public ClusterAverageMetric(DataComparator<DataMetric<E>, E> dataComparator,
                               DataMetric<E> dataMetric) {
      super(dataComparator, dataMetric);

      this.reset();
   }

   @Override
   public void apply(Cluster<E> data_A, Cluster<E> data_B) {
      for (E elem_A : data_A.getElements()) {
         for (E elem_B : data_B.getElements()) {
            mResult += mComparator.compare(mDataMetric, elem_A, elem_B);
            mLinkCount++;

            if (mDataMetric.getError() != 0) {
               setError(-1);
            }

            Logger.error(mDataMetric.getError(),
                         String.format("Error computing metric between " +
                                       "elements %s and %s\n", elem_A.getName(),
                                       elem_B.getName()));
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
      double result = mResult / mLinkCount;

      Logger.error(getError(), "Error computing ClusterAverageMetric");

      this.reset();
      return result;
   }
}
