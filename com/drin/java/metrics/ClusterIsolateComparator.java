package com.drin.java.metrics;

import com.drin.java.clustering.Cluster;
import com.drin.java.biology.Isolate;
import com.drin.java.types.DataObject;
import com.drin.java.metrics.ClusterComparator;
import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.IsolateMetric;

public class ClusterIsolateComparator extends ClusterComparator {
   private static final boolean DEBUG = false;

   @Override
   public Double compare(DataMetric<DataObject> isoMetric, Cluster<DataObject> clust_A,
    Cluster<DataObject> clust_B) {
      double comparisonSum = 0, comparisonCount = 0;

      for (DataObject obj_A : clust_A.getElements()) {
         if (obj_A instanceof Isolate) {
            Isolate iso_A = (Isolate) obj_A;

            for (DataObject obj_B : clust_B.getElements()) {
               if (obj_B instanceof Isolate) {
                  Isolate iso_B = (Isolate) obj_B;

                  isoMetric.apply(iso_A, iso_B);
               
                  Double comparison = isoMetric.result();
                  isoMetric.reset();

                  if (comparison != null) {
                     if (DEBUG) { System.out.printf("isolate comparison: %.04f\n", comparison.doubleValue()); }
                     comparisonSum += comparison.doubleValue();
                     comparisonCount++;
                  }

                  else {
                     if (DEBUG) {
                        System.out.printf("null comparison between isolates '%s' and '%s'\n",
                         iso_A.getName(), iso_B.getName());
                     }
                  }
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
   public boolean isSimilar(DataMetric<DataObject> isoMetric, Cluster<DataObject> clust_A,
    Cluster<DataObject> clust_B) {
      throw new UnsupportedOperationException();
   }
}
