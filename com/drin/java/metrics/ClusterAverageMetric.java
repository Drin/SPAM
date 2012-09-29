package com.drin.java.metrics;

import com.drin.java.types.DataObject;
import com.drin.java.clustering.Cluster;

public class ClusterAverageMetric extends ClusterMetric {
   private int mLinkCount;

   public ClusterAverageMetric(DataComparator dataComparator, DataMetric dataMetric) {
      super(dataComparator, dataMetric);
      mLinkCount = 0;
   }

   @Override
   public void apply(Cluster<DataObject> data_A, Cluster<DataObject> data_B) {
      double comparisonSum = 0;

      for (DataObject obj_A : data_A.getElements()) {
         for (DataObject obj_B : data_B.getElements()) {
            Double comparison = mComparator.compare(mDataMetric, data_A, data_B);

            if (comparison != null) {
               mLinkCount++;
               comparisonSum += comparison.doubleValue();
            }
         }
      }

      mResult = new Double(comparisonSum);
   }

   @Override
   public void reset() {
      mResult = null;
      mLinkCount = 0;
   }

   @Override
   public Double result() {
      return new Double(mResult.doubleValue()/mLinkCount);
   }
}
