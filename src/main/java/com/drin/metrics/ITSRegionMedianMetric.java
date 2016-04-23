package com.drin.metrics;

import com.drin.biology.ITSRegion;
import com.drin.biology.Pyroprint;

import com.drin.metrics.PyroprintMetric;

import java.util.List;
import java.util.ArrayList;

import com.drin.util.Logger;

public class ITSRegionMedianMetric extends DataMetric<ITSRegion> {
   private static final boolean TRANSFORM = false;
   private List<Double> mPyroComparisons;

   public ITSRegionMedianMetric(final double alpha, final double beta,
                                final ITSRegion appliedRegion,
                                final PyroprintMetric pyroMetric) {
      super(alpha, beta, appliedRegion, pyroMetric);
      this.reset();
   }

   public ITSRegionMedianMetric(final ITSRegion appliedRegion,
                                final PyroprintMetric pyroMetric) {
      super(appliedRegion, pyroMetric);
      this.reset();
   }

   @Override
   public void reset() {
      super.reset();
      mPyroComparisons = new ArrayList<Double>();
   }

   @Override
   public void apply(ITSRegion regionA, ITSRegion regionB) {
      if (regionA.getName().equals(regionB.getName())) {
         for (Pyroprint pyroA : regionA.getPyroprints()) {
            for (Pyroprint pyroB : regionB.getPyroprints()) {
               addComparison(pyroA.compareTo(pyroB));
            }
         }
      }
   }

   /**
    * Adds the new comparison to the list of pyroprint comparisons in sorted
    * (ascending) order.
    */
   private void addComparison(double newComparison) {
      int compareNdx = 0;

      for (; compareNdx < mPyroComparisons.size(); compareNdx++) {
         double tmp_val = mPyroComparisons.get(compareNdx).doubleValue();
         if (tmp_val > newComparison) { break; }
      }

      mPyroComparisons.add(compareNdx, newComparison);
   }

   @Override
   public double result() {
      double median = 0;

      if (!mPyroComparisons.isEmpty()) {
         int mid_ndx = mPyroComparisons.size()/2;
         median += mPyroComparisons.get(mid_ndx);

         if (mPyroComparisons.size() % 2 == 0) {
            median = (median + mPyroComparisons.get(mid_ndx + 1)) / 2;
         }
      }
      else { setError(-1); }

      Logger.error(getError(), "Error computing ITSRegionMedianMetric");

      this.reset();
      return median;
   }
}
