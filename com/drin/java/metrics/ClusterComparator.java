package com.drin.java.metrics;

import com.drin.java.types.Cluster;
import com.drin.java.types.DataObject;
import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.DataMetric;

public class ClusterComparator implements DataComparator<Cluster<DataObject>, DataMetric<DataObject>> {
   private static final boolean DEBUG = false;

   @Override
   public Double compare(DataMetric<DataObject> dataMetric, Cluster<DataObject> clust_A,
    Cluster<DataObject> clust_B) {
      double comparisonSum = 0, comparisonCount = 0;

      for (DataObject obj_A : clust_A.getElements()) {
         for (DataObject obj_B : clust_B.getElements()) {
            dataMetric.apply(obj_A, obj_B);

            Double comparison = dataMetric.result();
            dataMetric.reset();

            if (comparison != null) {
               comparisonSum += comparison.doubleValue();
               comparisonCount++;
            }
            else {
               if (DEBUG) {
                  System.out.printf("cluster comparator:\n\tnull comparison between '%s' and '%s' in comparator!\n",
                   obj_A.getName(), obj_B.getName());
               }
            }
         }
      }

      if (comparisonCount > 0) {
         return comparisonSum/comparisonCount;
      }

      return null;
   }

   @Override
   public boolean isSimilar(DataMetric<DataObject> dataMetric, Cluster<DataObject> clust_A,
    Cluster<DataObject> clust_B) {
      throw new UnsupportedOperationException();
   }
}
