package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.Threshold;
import com.drin.java.metrics.ITSRegionMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

public class ITSRegionAverageMetric extends ITSRegionMetric {
   private static final boolean TRANSFORM = false;
   private int mPairCount;

   public ITSRegionAverageMetric(ITSRegion region,
                                 PyroprintComparator pyroComp,
                                 PyroprintMetric pyroMetric) {
      super(region.getThreshold().getAlphaThreshold(),
            region.getThreshold().getBetaThreshold(), pyroComp, pyroMetric);

      reset();
   }

   public ITSRegionAverageMetric(PyroprintComparator pyroComp,
    PyroprintMetric pyroMetric) {
      super(pyroComp, pyroMetric);

      reset();
   }

   @Override
   public void reset() {
      mPairCount = 0;
      mResult = null;
   }

   @Override
   public void apply(Pyroprint elem_A, Pyroprint elem_B) {
      for (Pyroprint pyro_A : elem_A.getPyroprints()) {
         for (Pyroprint pyro_B : elem_B.getPyroprints()) {
            Double comparison = mPyroComp.compare(mPyroMetric, pyro_A, pyro_B);

            if (System.getenv().containsKey("DEBUG")) {
               if (comparison == null) {
                  System.out.printf("Pyroprint comparison is null!\n");
               }
               else if (comparison.isNaN()) {
                  System.out.printf("comparison is Nan and not null\n");
                  System.exit(1);
               }
            }

            if (comparison != null) {
               if (System.getenv().containsKey("DEBUG")) {
                  System.out.printf("ITSRegionAverageMetric:\n\tcomparison between " +
                                    "'%s' and '%s': %.04f\n", pyro_A.getName(),
                                    pyro_B.getName(), comparison.doubleValue());
               }

               mResult = new Double(mResult == null ? comparison.doubleValue()
                                                    : comparison.doubleValue() +
                                                      mResult.doubleValue());
               mPairCount++;
            }
         }
      }
   }

   @Override
   public Double result() {
      Double result = mResult;

      if (result != null) {
         if (System.getenv().containsKey("DEBUG")) {
            System.out.printf("ITSRegionAverageMetric %s: %.04f/%d\n",
                              (TRANSFORM ? "transformed" : "untransformed"),
                              mResult, mPairCount);
         }

         double tmp_result = (mResult.doubleValue()/mPairCount);
         result = new Double(TRANSFORM ? transformResult(tmp_result) : tmp_result);
      }

      reset();
      return result;
   }
}
