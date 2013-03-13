package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.biology.Pyroprint;

import com.drin.java.metrics.ITSRegionMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

import java.util.List;
import java.util.ArrayList;

import com.drin.java.util.Logger;

public class ITSRegionMedianMetric extends ITSRegionMetric {
   private static final boolean TRANSFORM = false;
   private List<Double> mPyroComparisons;

   public ITSRegionMedianMetric(double alpha, double beta,
                                ITSRegion appliedRegion,
                                PyroprintComparator pyroComp,
                                PyroprintMetric pyroMetric) {
      super(alpha, beta, appliedRegion, pyroComp, pyroMetric);
      this.reset();
   }

   public ITSRegionMedianMetric(ITSRegion appliedRegion,
                                PyroprintComparator pyroComp,
                                PyroprintMetric pyroMetric) {
      super(appliedRegion, pyroComp, pyroMetric);
      this.reset();
   }

   @Override
   public void reset() {
      super.reset();
      mPyroComparisons = new ArrayList<Double>();
   }

   @Override
   public void apply(ITSRegion elem_A, ITSRegion elem_B) {
      if (elem_A.getName().equals(elem_B.getName())) {
         for (Pyroprint pyro_A : elem_A.getPyroprints()) {
            for (Pyroprint pyro_B : elem_B.getPyroprints()) {
               addComparison(mPyroComp.compare(mPyroMetric, pyro_A, pyro_B));
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
         int low_ndx = mPyroComparisons.size()/2;
         int high_ndx = (mPyroComparisons.size()/2) + 1;

         median += mPyroComparisons.get(low_ndx);

         if (mPyroComparisons.size() % 2 == 0) {
            median = (median + mPyroComparisons.get(high_ndx)) / 2;
         }
      }
      else { setError(-1); }

      Logger.error(getError(), "Error computing ITSRegionMedianMetric");

      if (TRANSFORM) { median = transformResult(median); }

      this.reset();
      return median;
   }
}
