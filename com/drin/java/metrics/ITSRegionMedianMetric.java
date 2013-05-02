package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import java.util.List;
import java.util.ArrayList;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;

public class ITSRegionMedianMetric extends DataMetric<ITSRegion> {
   private static Configuration mConfig;
   private List<Double> mPyroComparisons;
   private String mRegionName;

   public ITSRegionMedianMetric() {
      super();

      mConfig = Configuration.getConfig();
   }

   @Override
   public void reset() {
      super.reset();
      mPyroComparisons = new ArrayList<Double>();
      mRegionName = "16-23";
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      if (elem_A.getName().equals(elem_B.getName())) {
         mRegionName = elem_A.getName();

         for (Pyroprint pyro_A : elem_A.getData()) {
            for (Pyroprint pyro_B : elem_B.getData()) {

               addComparison(pyro_A.compareTo(pyro_B));

            }
         }

      }
   }

   private void addComparison(double result) {
      int compareNdx = 0;

      for (; compareNdx < mPyroComparisons.size(); compareNdx++) {
         double tmp_val = mPyroComparisons.get(compareNdx).doubleValue();
         if (tmp_val - result > 0) { break; }
      }

      mPyroComparisons.add(compareNdx, result);
   }

   @Override
   public double result() {
      double median = 0;

      if (!mPyroComparisons.isEmpty()) {
         int low_ndx = (mPyroComparisons.size() - 1)/2;
         int high_ndx = ((mPyroComparisons.size() - 1)/2) + 1;

         median = mPyroComparisons.get(low_ndx);

         if (mPyroComparisons.size() % 2 == 0) {
            median = (median + mPyroComparisons.get(high_ndx)) / 2;
         }
      }
      else { setError(-1); }

      reset();
      return transformResult(median);
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
