package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;
import com.drin.java.util.Configuration;

import java.util.Collection;
import java.util.Iterator;

public class PyroprintStablePearsonMetric extends DataMetric<Pyroprint> {
   private static final int DEFAULT_LEN = 104;

   private int mPeakCount, mPyroLen;
   private double[] mPyro_A_arr, mPyro_B_arr;
   private double mPyro_A_sum, mPyro_B_sum;

   public PyroprintStablePearsonMetric() {
      super();

      Integer pyro_len = Configuration.getInt("PyroprintLength");
      mPyroLen = pyro_len == null ? DEFAULT_LEN : pyro_len.intValue();
   }

   public void reset() {
      mResult = 0;
      mErrCode = 0;

      mPyro_A_sum = 0;
      mPyro_B_sum = 0;

      mPyro_A_arr = new double[DEFAULT_LEN];
      mPyro_B_arr = new double[DEFAULT_LEN];
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

   public void apply(Pyroprint elem_A, Pyroprint elem_B) {
      if (!elem_A.hasSameProtocol(elem_B)) {
         Logger.debug(String.format("%s and %s have different protocols",
                                    elem_A.getName(), elem_B.getName()));
         return;
      }

      Logger.debug("Comparing pyroprints...");

      Collection<Double> peaks_A = elem_A.getData();
      Collection<Double> peaks_B = elem_B.getData();

      Iterator<Double> itr_A = peaks_A.iterator();
      Iterator<Double> itr_B = peaks_B.iterator();

      while (itr_A.hasNext() && itr_B.hasNext()) {
         double val_A = itr_A.next().doubleValue();
         double val_B = itr_B.next().doubleValue();

         stableCalc(val_A, val_B);
      }

      //If there wasn't an error during the calculation and a "stable"
      //correlation is desired...
      mResult = getStablePearson();
   }

   private double getStablePearson() {
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

   public double result() {
      double result = mResult;

      if (result == -2) { setError(-1); }

      Logger.error(mErrCode, String.format("Correlation: %.04f", result));

      reset();
      return result;
   }
}
