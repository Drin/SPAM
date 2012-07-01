package com.drin.java.metrics;

import com.drin.java.types.ITSRegion;
import com.drin.java.types.Pyroprint;
import com.drin.java.types.Threshold;
import com.drin.java.metrics.ITSRegionMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

public class ITSRegionAverageMetric extends ITSRegionMetric {
   private static final boolean DEBUG = false,
                                TRANSFORM = false;
   private int mPairCount;

   public ITSRegionAverageMetric(ITSRegion region,
    PyroprintComparator pyroComp, PyroprintMetric pyroMetric) {
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
   public void apply(Pyroprint data_A, Pyroprint data_B) {
      Double comparison = mPyroComp.compare(mPyroMetric, data_A, data_B);

      if (DEBUG) { if (comparison == null) { System.out.printf("Pyroprint comparison is null!\n"); } }

      if (DEBUG) {
         if (comparison.isNaN()) {
            System.out.printf("comparison is Nan and not null\n");
            System.exit(1);
         }
      }

      if (comparison != null && mResult != null) {
         if (DEBUG) {
            System.out.printf("ITSRegionAverageMetric:\n\tcomparison between '%s' and '%s': %.04f\n",
             data_A.getName(), data_B.getName(), comparison.doubleValue());
         }

         mResult = new Double(comparison.doubleValue() + mResult.doubleValue());
         mPairCount++;
      }

      else if (comparison != null) {
         if (DEBUG) {
            System.out.printf("ITSRegionAverageMetric:\n\tcomparison between '%s' and '%s': %.04f\n",
             data_A.getName(), data_B.getName(), comparison.doubleValue());
         }

         mResult = comparison;
         mPairCount++;

         /*
          * Sanity Check
          */
         if (mPairCount != 1) {
            System.err.println("Failed sanity check!");
            System.exit(1);
         }
      }
   }

   @Override
   public Double result() {
      if (mResult != null) {
         if (TRANSFORM) {
            if (DEBUG) {
               System.out.printf("ITSRegionAverageMetric transformed:\n\tResult: %.04f/%d\n",
                mResult, mPairCount);
            }

            return new Double(transformResult(mResult.doubleValue()/mPairCount));
         }
         else {
            if (DEBUG) {
               System.out.printf("ITSRegionAverageMetric untransformed:\n\tResult: %.04f/%d\n",
                mResult, mPairCount);
            }

            return new Double(mResult.doubleValue()/mPairCount);
         }
      }

      reset();

      return mResult;
   }
}
