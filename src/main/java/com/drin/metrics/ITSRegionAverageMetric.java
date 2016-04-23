package com.drin.metrics;

import com.drin.biology.ITSRegion;
import com.drin.biology.Pyroprint;

import com.drin.metrics.DataMetric;

import com.drin.util.Configuration;
import com.drin.util.Logger;
import com.drin.util.InvalidPropertyException;

public class ITSRegionAverageMetric extends DataMetric<ITSRegion> {
   private int mPairCount;
   private double mAlpha, mBeta;

   public ITSRegionAverageMetric(double alpha, double beta) {
      this.reset();

      mAlpha = alpha;
      mBeta = beta;
   }

   @Override
   public void reset() {
      super.reset();
      mPairCount = 0;
   }

   @Override
   public void apply(ITSRegion regionA, ITSRegion regionB) {
      if (regionA.getName().equals(regionB.getName())) {

         for (Pyroprint pyroA : regionA.getData()) {
            for (Pyroprint pyroB : regionB.getData()) {
               double result = pyroA.compareTo(pyroB);

               Logger.debug(String.format("ITSRegionAverageMetric:\n\t" +
                                          "comparison between '%s' and " +
                                          "'%s' [%d]: %.04f", pyroA.getName(),
                                          pyroB.getName(), pyroA.getDispLen(),
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
      return result;
   }

   private double transformResult(double result) {
      if (result > mAlpha) { return 1; }
      else if (result < mBeta) { return 0; }
      return result;
   }
}
