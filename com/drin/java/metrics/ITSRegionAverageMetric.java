package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

public class ITSRegionAverageMetric extends DataMetric<ITSRegion> {
   private static Configuration mConfig;
   private String mRegionName;
   private int mPairCount;

   public ITSRegionAverageMetric() {
      super();

      mConfig = Configuration.getConfig();
   }

   @Override
   public void reset() {
      super.reset();
      mPairCount = 0;
      mRegionName = "16-23";
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

      reset();

      return transformResult(result);
   }

   private double transformResult(double result) {
      String alphaStr = null, betaStr = null;

      if (Boolean.parseBoolean(mConfig.getAttr(Configuration.TRANSFORM_KEY))) {
         try {
            alphaStr = mConfig.getRegionAttr(mRegionName, Configuration.ALPHA_KEY);
            betaStr = mConfig.getRegionAttr(mRegionName, Configuration.BETA_KEY); 

            double alphaThresh = Double.parseDouble(alphaStr);
            double betaThresh = Double.parseDouble(betaStr);

            if (result >= alphaThresh) { return 1; }
            else if (result < betaThresh) { return 0; }
         }
         catch(NumberFormatException numErr) {
            mErrCode = -1;
            Logger.error(mErrCode, String.format("Invalid threshold value " +
                                                 "(%s or %s) for " +
                                                 "region %s\n", mRegionName,
                                                 alphaStr, betaStr));
            numErr.printStackTrace();
         }
      }

      return result;
   }
}
