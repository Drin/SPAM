package com.drin.java.metrics;

import com.drin.java.metrics.PyroprintMetric;

public class PyroprintPearsonMetric extends PyroprintMetric {
   private static final int DEFAULT_LEN = 104;
   private static final boolean USE_STABLE = false,
                                USE_DISTANCE = false,
                                DEBUG = false;
   private double[] mPyro_A_arr, mPyro_B_arr;
   private double mPyro_A_sum, mPyro_B_sum, mProduct_AB,
                  mPyro_A_squared_sum, mPyro_B_squared_sum;
   private int mPeakCount;

   public PyroprintPearsonMetric() {
      super();
      reset();
   }

   @Override
   public void reset() {
      mPyro_A_sum = 0;
      mPyro_B_sum = 0;

      mPyro_A_squared_sum = 0;
      mPyro_B_squared_sum = 0;

      mPeakCount = 0;
      mProduct_AB = 0;

      mPyro_A_arr = new double[DEFAULT_LEN];
      mPyro_B_arr = new double[DEFAULT_LEN];
   }

   private void unstableCalc(double peak_A, double peak_B) {
      mPyro_A_sum += peak_A;
      mPyro_B_sum += peak_B;
      
      mPyro_A_squared_sum += peak_A * peak_A;
      mPyro_B_squared_sum += peak_B * peak_B;
      
      mProduct_AB += peak_A * peak_B;
      mPeakCount++;
   }

   private void stableCalc(double peak_A, double peak_B) {
      if (DEBUG) {
         System.err.printf("index: %d peak_A: %.04f peak_B: %.04f\n",
          mPeakCount, peak_A, peak_B);
      }

      mPyro_A_arr[mPeakCount] = peak_A;
      mPyro_B_arr[mPeakCount] = peak_B;

      mPyro_A_sum += peak_A;
      mPyro_B_sum += peak_B;
      
      mPeakCount++;
   }

   @Override
   public void apply(Double elem_A, Double elem_B) {
      if (elem_A != null && elem_B != null) {
         if (USE_STABLE) { stableCalc(elem_A.doubleValue(), elem_B.doubleValue()); }
         else { unstableCalc(elem_A.doubleValue(), elem_B.doubleValue()); }
      }
      else { System.out.printf("elem_A or elem_B is null!\n"); }
   }

   private void debugState() {
      System.out.printf("numElements: %d, pyro_A_sum: %.04f, pyro_B_sum: %.04f," +
       " pyroA_squared_sum: %.04f, pyroB_squared_sum: %.04f\n", mPeakCount,
       mPyro_A_sum, mPyro_B_sum, mPyro_A_squared_sum, mPyro_B_squared_sum);
   }

   private Double getUnstablePearsonDistance() {
      return new Double(1 - getUnstablePearsonSimilarity().doubleValue());
   }

   private Double getUnstablePearsonSimilarity() {
      if (DEBUG) { debugState(); }
      if (mPeakCount == 0) {
         return new Double(-1);
      }

      return new Double((((mPeakCount * mProduct_AB) - (mPyro_A_sum * mPyro_B_sum))/
                         (Math.sqrt((mPeakCount * mPyro_A_squared_sum) - (mPyro_A_sum * mPyro_A_sum)) *
                          Math.sqrt((mPeakCount * mPyro_B_squared_sum) - (mPyro_B_sum * mPyro_B_sum)))));
   }

   private Double getStablePearsonDistance() {
      return new Double(1 - getStablePearsonSimilarity().doubleValue());
   }

   private Double getStablePearsonSimilarity() {
      double mean_A = mPyro_A_sum/mPeakCount, mean_B = mPyro_A_sum/mPeakCount;
      double stdDev_A = 0, stdDev_B = 0, coVar = 0;

      for (int tmpNdx = 0; tmpNdx < mPeakCount; tmpNdx++) {
         double resid_A = mPyro_A_arr[tmpNdx] - mean_A;
         double resid_B = mPyro_B_arr[tmpNdx] - mean_B;

         coVar += resid_A * resid_B;

         stdDev_A += resid_A * resid_A;
         stdDev_B += resid_B * resid_B;
      }

      return new Double(coVar/(Math.sqrt(stdDev_A) * Math.sqrt(stdDev_B)));
   }

   @Override
   public Double result() {
      Double result = null;

      if (USE_STABLE) {
         if (USE_DISTANCE) { result = getStablePearsonDistance(); }
         else { result = getStablePearsonSimilarity(); }
      }

      else {
         if (USE_DISTANCE) { result = getUnstablePearsonDistance(); }
         else {
            result = getUnstablePearsonSimilarity();
            if (DEBUG) { System.out.printf("Pearson Correlation:\n\tresult: %.04f\n", result); }
         }
      }

      reset();
      return result;
   }
}
