package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.ITSRegionMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

import com.drin.java.util.Logger;

public class ITSRegionAverageMetric extends ITSRegionMetric {
   private static final boolean TRANSFORM = false;
   private int mPairCount;

   public ITSRegionAverageMetric(double alpha, double beta,
                                 ITSRegion appliedRegion,
                                 PyroprintComparator pyroComp,
                                 PyroprintMetric pyroMetric) {
      super(alpha, beta, appliedRegion, pyroComp, pyroMetric);
      this.reset();
   }

   public ITSRegionAverageMetric(ITSRegion appliedRegion,
                                 PyroprintComparator pyroComp,
                                 PyroprintMetric pyroMetric) {
      super(appliedRegion, pyroComp, pyroMetric);
      this.reset();
   }

   @Override
   public void reset() {
      mPairCount = mErrCode = 0;
      mResult = 0;
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      if (elem_A.isSimilarRegion(elem_B)) {
         for (Pyroprint pyro_A : elem_A.getPyroprints()) {
            for (Pyroprint pyro_B : elem_B.getPyroprints()) {
               double result = mPyroComp.compare(mPyroMetric, pyro_A, pyro_B);

               Logger.error(mPyroMetric.getError(),
                            "Error computing ITSRegion metric\n");
               Logger.debug(String.format("ITSRegionAverageMetric:\n\t" +
                                          "comparison between '%s' and " +
                                          "'%s': %.04f", pyro_A.getName(),
                                          pyro_B.getName(), result));

               mResult += result;
               mPairCount++;
            }
         }
      }
   }

   @Override
   public double result() {
      double result = mResult/mPairCount;

      Logger.debug(String.format("ITSRegionAverageMetric %s: %.04f/%d",
                                 (TRANSFORM ? "(t)" : "(not t)"),
                                 mResult, mPairCount));

      if (TRANSFORM) { result = transformResult(result); }

      reset();
      return result;
   }
}
