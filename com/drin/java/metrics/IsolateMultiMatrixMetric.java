package com.drin.java.metrics;

import com.drin.java.biology.Isolate;
import com.drin.java.biology.ITSRegion;
import com.drin.java.metrics.Threshold;
import com.drin.java.metrics.DataMetric;
import com.drin.java.util.Logger;

import java.util.Map;
import java.util.HashMap;

public class IsolateMultiMatrixMetric implements DataMetric<Isolate> {
   private static final boolean DEBUG = false;

   protected Map<String, Map<String, Map<String, Double>>> mMatrixMap;
   protected Map<String, Threshold> mThresholdMap;

   protected Double mResult;

   public IsolateMultiMatrixMetric() {
      mMatrixMap = new HashMap<String, Map<String, Map<String, Double>>>();
      mThresholdMap = new HashMap<String, Threshold>();
      mResult = null;
   }

   public void reset() {
      mResult = null;
   }

   public void addIsolateMatrix(ITSRegion region, Map<String, Map<String, Double>> matrix) {
      mMatrixMap.put(region.getName(), matrix);
   }

   public void addThreshold(ITSRegion region, Threshold thresh) {
      mThresholdMap.put(region.getName(), thresh);
   }

   protected void missingMapping(Isolate elem_A, Isolate elem_B) {
      System.err.printf("Similarity Matrix missing mapping '%s -> %s'\n",
       elem_A.getName(), elem_B.getName());

      System.exit(1);
   }

   @Override
   public void apply(Isolate elem_A, Isolate elem_B) {
      double comparison = 0, comparisonCount = 0;

      for (String regionName : mMatrixMap.keySet()) {
         Map<String, Map<String, Double>> matrix = mMatrixMap.get(regionName);

         if (DEBUG) { System.out.printf("(%s) %s : %s\n", regionName, elem_A, elem_B); }

         if (matrix != null) {
            if (elem_A.equals(elem_B)) { return; }
      
            else if (matrix.containsKey(elem_A.getName()) &&
             matrix.get(elem_A.getName()).containsKey(elem_B.getName())) {
               mResult = matrix.get(elem_A.getName()).get(elem_B.getName());
            }
      
            else if (matrix.containsKey(elem_B.getName()) &&
             matrix.get(elem_B.getName()).containsKey(elem_A.getName())) {
               mResult = matrix.get(elem_B.getName()).get(elem_A.getName());
            }
      
            else { missingMapping(elem_A, elem_B); }
         }
      
         if (DEBUG) { System.out.printf("(%s) %s : %s -> %.08f\n",
          regionName, elem_A, elem_B, mResult.doubleValue()); }

         Double result = this.transformedResult(regionName);
         comparisonCount++;

         if (result != null && result.doubleValue() > 0) {
            comparison += result.doubleValue();
         }

         else {
            comparison = 0;
            break;
         }
      }

      if (DEBUG) {
         System.out.printf("%.04f/%f = %.04f\n", comparison,
          comparisonCount, comparison/comparisonCount);
      }

      mResult = new Double(comparison/comparisonCount);
   }

   @Override
   public Double result() {
      if (mResult == null) {Logger.debug("Cluster metric has no result to report!"); }
      return mResult;
   }

   public Double transformedResult(String regionName) {
      Threshold thresholdPair = mThresholdMap.get(regionName);

      if (mResult != null) {
         if (mResult.doubleValue() >= thresholdPair.getAlphaThreshold()) {
            return new Double(1);
         }

         else if (mResult.doubleValue() < thresholdPair.getBetaThreshold()) {
            return new Double(0);
         }
      }

      return mResult;
   }
}
