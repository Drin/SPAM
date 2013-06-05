package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Logger;
import com.drin.java.util.Configuration;

import com.drin.java.util.InvalidPropertyException;

import java.util.Collection;
import java.util.Iterator;

public class PyroprintUnstablePearsonMetric extends DataMetric<Pyroprint> {
   private static final int DEFAULT_LEN = 104;

   private int mPeakCount, mPyroLen;
   private float mPyro_A_sum, mPyro_B_sum, mProduct_AB,
                  mPyro_A_squared_sum, mPyro_B_squared_sum;

   public PyroprintUnstablePearsonMetric() {
      super();

      Integer pyro_len = Configuration.getInt("PyroprintLength");
      mPyroLen = pyro_len == null ? DEFAULT_LEN : pyro_len.intValue();
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

   private void unstableCalc(float peak_A, float peak_B) {
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

      Logger.debug("Comparing pyroprints...");

      Collection<Float> peaks_A = elem_A.getData();
      Collection<Float> peaks_B = elem_B.getData();

      Iterator<Float> itr_A = peaks_A.iterator();
      Iterator<Float> itr_B = peaks_B.iterator();

      while (itr_A.hasNext() && itr_B.hasNext()) {
         float val_A = itr_A.next().floatValue();
         float val_B = itr_B.next().floatValue();

         unstableCalc(val_A, val_B);
      }

      //If there wasn't an error during the calculation and an "unstable"
      //correlation is desired...
      mResult = getUnstablePearson();
   }

   private String debugState() {
      return String.format("numElements: %d, pyro_A_sum: %.06f, pyro_B_sum: " +
                           "%.06f, pyroA_squared_sum: %.06f, " +
                           "pyroB_squared_sum: %.06f, product_AB: %.06f",
                           mPeakCount, mPyro_A_sum, mPyro_B_sum,
                           mPyro_A_squared_sum, mPyro_B_squared_sum,
                           mProduct_AB);
   }

   private float getUnstablePearson() {
      Logger.debug(debugState());

      if (mPeakCount == 0) { return -2; }

      float pearson_numerator = (mPeakCount * mProduct_AB) - (mPyro_A_sum * mPyro_B_sum);
      float denom_A = (mPeakCount * mPyro_A_squared_sum) - (mPyro_A_sum * mPyro_A_sum);
      float denom_B = (mPeakCount * mPyro_B_squared_sum) - (mPyro_B_sum * mPyro_B_sum);

      return (pearson_numerator/(float) Math.sqrt(denom_A * denom_B));
   }

   public float result() {
      float result = mResult;

      if (result == -2) { setError(-1); }

      reset();
      return result;
   }
}
