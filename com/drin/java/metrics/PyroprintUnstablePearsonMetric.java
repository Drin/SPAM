package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;

import java.util.Collection;
import java.util.Iterator;

public class PyroprintUnstablePearsonMetric extends DataMetric<Pyroprint> {
   private int mPeakCount;
   private double mPyro_A_sum, mPyro_B_sum, mProduct_AB,
                  mPyro_A_squared_sum, mPyro_B_squared_sum;

   public PyroprintUnstablePearsonMetric() {
      super();
   }

   public void reset() {
      mResult = 0;
      mErrCode = 0;

      mPyro_A_sum = 0;
      mPyro_B_sum = 0;

      mPyro_A_squared_sum = 0;
      mPyro_B_squared_sum = 0;

      mPeakCount = 0;
      mProduct_AB = 0;
   }

   private void unstableCalc(double peak_A, double peak_B) {
      mPyro_A_sum += peak_A;
      mPyro_B_sum += peak_B;
      
      mPyro_A_squared_sum += peak_A * peak_A;
      mPyro_B_squared_sum += peak_B * peak_B;
      
      mProduct_AB += peak_A * peak_B;
      mPeakCount++;
   }

   public void apply(Pyroprint elem_A, Pyroprint elem_B) {
      if (!elem_A.hasSameProtocol(elem_B)) {
         Logger.debug(String.format("%s and %s have different protocols",
                                    elem_A.getName(), elem_B.getName()));
         return;
      }

      /*
      Logger.debug("Comparing pyroprints...");
      */

      Collection<Double> peaks_A = elem_A.getData();
      Collection<Double> peaks_B = elem_B.getData();

      Iterator<Double> itr_A = peaks_A.iterator();
      Iterator<Double> itr_B = peaks_B.iterator();

      while (itr_A.hasNext() && itr_B.hasNext()) {
         double val_A = itr_A.next().doubleValue();
         double val_B = itr_B.next().doubleValue();

         unstableCalc(val_A, val_B);
      }

      //If there wasn't an error during the calculation and an "unstable"
      //correlation is desired...
      mResult = getUnstablePearson();
   }

   @SuppressWarnings("unused")
   private String debugState() {
      return String.format("numElements: %d, pyro_A_sum: %.06f, pyro_B_sum: " +
                           "%.06f, pyroA_squared_sum: %.06f, " +
                           "pyroB_squared_sum: %.06f, product_AB: %.06f",
                           mPeakCount, mPyro_A_sum, mPyro_B_sum,
                           mPyro_A_squared_sum, mPyro_B_squared_sum,
                           mProduct_AB);
   }

   private double getUnstablePearson() {
      //Logger.debug(debugState());

      if (mPeakCount == 0) { return -2; }

      double pearson_numerator = (mPeakCount * mProduct_AB) - (mPyro_A_sum * mPyro_B_sum);
      double denom_A = (mPeakCount * mPyro_A_squared_sum) - (mPyro_A_sum * mPyro_A_sum);
      double denom_B = (mPeakCount * mPyro_B_squared_sum) - (mPyro_B_sum * mPyro_B_sum);

      return (pearson_numerator/Math.sqrt(denom_A * denom_B));
   }

   public double result() {
      double result = mResult;

      if (result == -2) { setError(-1); }

      reset();
      return result;
   }
}
