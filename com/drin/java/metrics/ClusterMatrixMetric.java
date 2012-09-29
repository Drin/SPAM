package com.drin.java.metrics;

import com.drin.java.types.DataObject;
import com.drin.java.clustering.Cluster;
import com.drin.java.biology.Isolate;
import com.drin.java.metrics.DataComparator;
import com.drin.java.metrics.IsolateMultiMatrixMetric;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;

public class ClusterMatrixMetric implements DataMetric<Cluster<DataObject>> {
   private static final boolean DEBUG = false;
   private Map<String, Map<String, Double>> mMatrix;

   private Double mResult;

   public ClusterMatrixMetric() {
      mMatrix = new HashMap<String, Map<String, Double>>();

      mResult = null;
   }

   public void reset() {
      mResult = null;
   }

   public void buildMatrix(DataComparator<Cluster<DataObject>, DataMetric<DataObject>> dataComparator,
    DataMetric metric, Set<Cluster> dataSet) {
      DataMetric<DataObject> dataMetric = (DataMetric<DataObject>) metric;

      for (Cluster cluster_A : dataSet) {
         Cluster<DataObject> clust_A = (Cluster<DataObject>) cluster_A;

         if (!mMatrix.containsKey(clust_A.getName())) {
            mMatrix.put(clust_A.getName(), new HashMap<String, Double>());
         }
      
         Map<String, Double> corrMap = mMatrix.get(clust_A.getName());
      
         for (Cluster cluster_B : dataSet) {
            Cluster<DataObject> clust_B = (Cluster<DataObject>) cluster_B;

            if (!clust_A.equals(clust_B)) {
               Double comparison = dataComparator.compare(dataMetric, clust_A, clust_B);
               corrMap.put(clust_B.getName(), comparison);
            }
         }
      }

      if (DEBUG) { printMapping(mMatrix); }
   }

   private void printMapping(Map<String, Map<String, Double>> matrix) {
      for (String key_A : matrix.keySet()) {
         Map<String, Double> mapping = matrix.get(key_A);

         for (String key_B : mapping.keySet()) {
            System.out.printf("'%s' -> '%s': %.08f (null: %s)\n", key_A, key_B,
             mapping.get(key_B), mapping.get(key_B) == null);
         }
      }
   }

   public void recompute(Cluster<DataObject> data_A, Cluster<DataObject> data_B) {
      double comparison = 0, comparisonCount = 0;

      if (DEBUG) {
         System.out.printf("recomputing between elements of '%s' and cluster '%s'\n",
          data_A.getName(), data_B.getName());
      }

      for (DataObject element_A : data_A.getElementList()) {
         if (DEBUG) {
            System.out.printf("comparing element '%s' and '%s'\n", element_A.getName(),
             data_B.getName());
         }

         if (element_A.getName().equals(data_B.getName())) { continue; }

         if (mMatrix.containsKey(element_A.getName()) && mMatrix.get(element_A.getName())
          .containsKey(data_B.getName())) {
            Double compValue = mMatrix.get(element_A.getName()).get(data_B.getName());

            if (compValue != null) {
               comparison += compValue.doubleValue();
               comparisonCount++;
            }

            if (DEBUG) {
               if (compValue == null) {
                  System.err.printf("Null mapping between '%s' and '%s'\n",
                   element_A.getName(), data_B.getName());
               
                  Double otherCompValue = mMatrix.get(data_B.getName()).get(element_A.getName());
                  if (otherCompValue == null) {
                     System.err.printf("Null mapping between '%s' and '%s' too\n",
                      data_B.getName(), element_A.getName());
                  }
               }
            }
         }
      
         else if (mMatrix.containsKey(data_B.getName()) && mMatrix.get(data_B.getName())
          .containsKey(element_A.getName())) {
            comparison += mMatrix.get(data_B.getName()).get(element_A.getName()).doubleValue();
            comparisonCount++;
         }

         else {
            if (DEBUG) {
               System.err.printf("No mapping between '%s' and '%s' when recomputing\n",
                element_A.getName(), data_B.getName());
            }
         }
      }

      if (comparisonCount > 0) {
         Double newValue = new Double(comparison/comparisonCount);
         mMatrix.get(data_A.getName()).put(data_B.getName(), newValue);

         //mMatrix.get(data_B.getName()).put(data_A.getName(), null);

         if (DEBUG) {
            System.out.printf("'%s' -> '%s': %.04f (null: %s)\n",
             data_A.getName(), data_B.getName(), newValue.doubleValue(),
             newValue == null);
         }
      }

      else {
         //mMatrix.get(data_A.getName()).put(data_B.getName(), null);
         //mMatrix.get(data_B.getName()).put(data_A.getName(), null);

         if (DEBUG) {
            System.err.printf("No mapping between '%s' and '%s' after recomputing\n",
             data_A.getName(), data_B.getName());
         }
      }
   }

   @Override
   public void apply(Cluster data_A, Cluster data_B) {
      if (mMatrix.containsKey(data_A.getName()) && mMatrix.get(data_A.getName())
       .containsKey(data_B.getName())) {
         mResult = mMatrix.get(data_A.getName()).get(data_B.getName());
      }

      else if (mMatrix.containsKey(data_B.getName()) && mMatrix.get(data_B.getName())
       .containsKey(data_A.getName())) {
         mResult = mMatrix.get(data_B.getName()).get(data_A.getName());
      }

      else {
         System.err.printf("Missing mapping between '%s' and '%s'. Verify matrix has been built!\n",
          data_A.getName(), data_B.getName());
      }
   }

   @Override
   public Double result() {
      return mResult;
   }
}
