package com.drin.java.metrics;

import com.drin.java.types.Isolate;
import com.drin.java.metrics.DataMetric;
import com.drin.java.util.Logger;

import java.util.Map;

public class IsolateMatrixMetric implements DataMetric<Isolate> {
   private static final double DEFAULT_ALPHA = .997,
                               DEFAULT_BETA = .95;
   private static final boolean DEBUG = false;

   protected Map<String, Map<String, Double>> mMatrix;
   protected Double mResult, mAlphaThreshold, mBetaThreshold;

   public IsolateMatrixMetric(Map<String, Map<String, Double>> matrix) {
      this(matrix, DEFAULT_ALPHA, DEFAULT_BETA);
   }

   public IsolateMatrixMetric(Map<String, Map<String, Double>> matrix,
    double alpha, double beta) {
      mMatrix = matrix;
      mAlphaThreshold = alpha;
      mBetaThreshold = beta;
      mResult = null;
   }

   public void reset() {
      mResult = null;
   }

   protected void missingMapping(Isolate elem_A, Isolate elem_B) {
      System.err.printf("Similarity Matrix missing mapping '%s -> %s'\n",
       elem_A.getName(), elem_B.getName());

      System.exit(1);
   }

   @Override
   public void apply(Isolate elem_A, Isolate elem_B) {
      if (mMatrix != null) {
         if (elem_A.equals(elem_B)) { return; }

         else if (mMatrix.containsKey(elem_A.getName()) &&
          mMatrix.get(elem_A.getName()).containsKey(elem_B.getName())) {
            mResult = mMatrix.get(elem_A.getName()).get(elem_B.getName());
         }

         else if (mMatrix.containsKey(elem_B.getName()) &&
          mMatrix.get(elem_B.getName()).containsKey(elem_A.getName())) {
            mResult = mMatrix.get(elem_B.getName()).get(elem_A.getName());
         }

         else { missingMapping(elem_A, elem_B); }
      }

      if (DEBUG) { System.out.printf("%s : %s -> %.04f\n", elem_A, elem_B, mResult.doubleValue()); }
   }

   @Override
   public Double result() {
      if (mResult == null) {Logger.debug("Cluster metric has no result to report!"); }
      return mResult;
   }

   public Double transformedResult() {
      if (mResult != null) {
         if (mResult.doubleValue() >= mAlphaThreshold) {
            return new Double(1);
         }

         else if (mResult.doubleValue() < mBetaThreshold) {
            return new Double(0);
         }
      }

      return mResult;
   }
}
