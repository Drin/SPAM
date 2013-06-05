package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.DataMetric;

import com.drin.java.util.Configuration;
import com.drin.java.util.Logger;
import com.drin.java.util.InvalidPropertyException;

public class ITSRegionAverageMetric extends DataMetric<ITSRegion> {
   private static final String TRANSFORM_KEY = "TransformCorrelation";

   private Boolean mTransform;
   private int mPairCount;
   private float mAlpha, mBeta;

   public ITSRegionAverageMetric() {
      this.reset();

      mAlpha = 0.995f;
      mBeta = 0.99f;

      mTransform = Configuration.getBoolean(TRANSFORM_KEY);
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
               float result = pyro_A.compareTo(pyro_B);

               Logger.debug(String.format("ITSRegionAverageMetric:\n\t" +
                                          "comparison between '%s' and " +
                                          "'%s' [%d]: %.04f", pyro_A.getName(),
                                          pyro_B.getName(), pyro_A.getPyroLen(),
                                          result));

               mResult += result;
               mPairCount++;
            }
         }

      }
   }

   @Override
   public float result() {
      float result = mResult;

      if (mPairCount <= 0) { setError(-1); }
      else { result /= mPairCount; }

      if (mTransform != null) {
         Logger.debug(String.format("ITSRegionAverageMetric %s: %.04f/%d",
                                    (mTransform ? "(t)" : "(not t)"),
                                    mResult, mPairCount));

         if (mTransform) { result = transformResult(result); }
      }
      else { Logger.error(-1, "Invalid Transform Property"); }

      reset();
      return result;
   }

   private float transformResult(float result) {
      if (result > mAlpha) { return 1; }
      else if (result < mBeta) { return 0; }
      return result;
   }
}
