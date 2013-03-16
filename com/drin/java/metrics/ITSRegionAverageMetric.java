package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

public class ITSRegionAverageMetric extends DataMetric<ITSRegion> {
   private int mPairCount;
   private Configuration mConfig;

   public ITSRegionAverageMetric() {
      this.reset();

      mConfig = Configuration.loadConfig();
   }

   @Override
   public void reset() {
      super.reset();
      mPairCount = 0;
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      if (elem_A.getName().equals(elem_B.getName())) {

         for (Pyroprint pyro_A : elem_A.getData()) {
            for (Pyroprint pyro_B : elem_B.getData()) {
               double result = pyro_A.compareTo(pyro_B);

               Logger.debug(String.format("ITSRegionAverageMetric:\n\t" +
                                          "comparison between '%s' and " +
                                          "'%s' [%d]: %.04f", pyro_A.getName(),
                                          pyro_B.getName(), pyro_A.getDispLen(),
                                          result));

               mResult += result;
               mPairCount++;
            }
         }

      }
   }

   @Override
   public double result() {
      double result = mResult;

      if (mPairCount <= 0) { setError(-1); }
      else { result /= mPairCount; }

      if (mConfig.getTransform() != null) {
         Logger.debug(String.format("ITSRegionAverageMetric %s: %.04f/%d",
                                    (mConfig.getTransform() ? "(t)" : "(not t)"),
                                    mResult, mPairCount));

         if (mConfig.getTransform()) { result = transformResult(result); }
      }
      else { Logger.error(-1, "Invalid Transform Property"); }

      reset();
      return result;
   }

   private double transformResult(double result) {
      /* TODO
      if (result > mAlpha) { return 1; }
      else if (result < mBeta) { return 0; }
      */
      return result;
   }
}
