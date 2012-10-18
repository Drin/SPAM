package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.PyroprintMetric;

import com.drin.java.util.Logger;

import java.util.List;

public class PyroprintPearsonMetric extends PyroprintMetric {
   private static final int DEFAULT_LEN = 104;
   private static final boolean USE_STABLE = false,
                                USE_DISTANCE = false;

   private int mPeakCount;
   private double[] mPyro_A_arr, mPyro_B_arr;
   private double mPyro_A_sum, mPyro_B_sum, mProduct_AB,
                  mPyro_A_squared_sum, mPyro_B_squared_sum;

   public PyroprintPearsonMetric() { super(); }

   @Override
   public void reset() {
      mResult = -1;
      mErrCode = 0;

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
      Logger.debug(String.format("index: %d peak_A: %.04f peak_B: %.04f",
                                 mPeakCount, peak_A, peak_B));

      mPyro_A_arr[mPeakCount] = peak_A;
      mPyro_B_arr[mPeakCount] = peak_B;

      mPyro_A_sum += peak_A;
      mPyro_B_sum += peak_B;
      
      mPeakCount++;
   }

   @Override
   public void apply(Pyroprint elem_A, Pyroprint elem_B) {
      if (!elem_A.hasSameProtocol(elem_B)) {
         System.out.printf("%s and %s have different protocols",
                           elem_A.getName(), elem_B.getName());
         return;
      }

      Logger.debug("Comparing pyroprints...");

      List<Double> peakList_A = elem_A.getPeaks();
      List<Double> peakList_B = elem_B.getPeaks();

      for (int ndx = 0; ndx < elem_A.getLength(); ndx++) {
         double val_A = peakList_A.get(ndx).doubleValue();
         double val_B = peakList_B.get(ndx).doubleValue();

         if (USE_STABLE) { stableCalc(val_A, val_B); }
         else { unstableCalc(val_A, val_B); }
      }

      //If there wasn't an error during the calculation and a "stable"
      //correlation is desired...
      if (USE_STABLE) {
         mResult = (USE_DISTANCE ? getStablePearsonDistance()
                                 : getStablePearsonSimilarity());
      }
      //If there wasn't an error during the calculation and an "unstable"
      //correlation is desired...
      else {
         mResult = (USE_DISTANCE ? getUnstablePearsonDistance()
                                 : getUnstablePearsonSimilarity());
      }
   }

   private void debugState() {
      System.out.printf("numElements: %d, pyro_A_sum: %.04f, pyro_B_sum: %.04f," +
       " pyroA_squared_sum: %.04f, pyroB_squared_sum: %.04f", mPeakCount,
       mPyro_A_sum, mPyro_B_sum, mPyro_A_squared_sum, mPyro_B_squared_sum);
   }

   private double getUnstablePearsonDistance() {
      return (1 - getUnstablePearsonSimilarity());
   }

   private double getStablePearsonDistance() {
      return (1 - getStablePearsonSimilarity());
   }

   private double getUnstablePearsonSimilarity() {
      Logger.debug(debugState());

      if (mPeakCount == 0) { return -2; }

      double pearson_numerator = (mPeakCount * mProduct_AB) - (mPyro_A_sum * mPyro_B_sum);
      double denom_A = (mPeakCount * mPyro_A_squared_sum) - (mPyro_A_sum * mPyro_A_sum);
      double denom_B = (mPeakCount * mPyro_B_squared_sum) - (mPyro_B_sum * mPyro_B_sum);

      return (pearson_numerator/Math.sqrt(denom_A * denom_B));
   }

   private double getStablePearsonSimilarity() {
      if (mPeakCount == 0) {
         double mean_A = mPyro_A_sum/mPeakCount;
         double mean_B = mPyro_B_sum/mPeakCount;
         double stdDev_A = 0, stdDev_B = 0, coVar = 0;

         for (int tmpNdx = 0; tmpNdx < mPeakCount; tmpNdx++) {
            double resid_A = mPyro_A_arr[tmpNdx] - mean_A;
            double resid_B = mPyro_B_arr[tmpNdx] - mean_B;

            coVar += resid_A * resid_B;

            stdDev_A += resid_A * resid_A;
            stdDev_B += resid_B * resid_B;
         }

         return (coVar/Math.sqrt(stdDev_A * stdDev_B));
      }

      return -2;
   }

   @Override
   public double result() {
      double result = mResult;

      if (result == -2) { setError(-1); }

      Logger.error(mErrCode, String.format("Correlation: %.04f", result));

      reset();
      return result;
   }
}
