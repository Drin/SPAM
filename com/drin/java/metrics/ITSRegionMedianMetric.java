package com.drin.java.metrics;

import com.drin.java.metrics.ITSRegionMetric;

public class ITSRegionMedianMetric extends ITSRegionMetric {
   private List<Double> mPyroComparisons;

   public ITSRegionMedianMetric(double alphaThreshold, double betaThreshold,
    PyroprintComparator pyroComp, PyroprintMetric pyroMetric) {
      super(alphaThreshold, betaThreshold, pyroComp, pyroMetric);
      mPairCount = 0;
   }

   public ITSRegionMedianMetric(PyroprintComparator pyroComp,
    PyroprintMetric pyroMetric) {
      super(pyroComp, pyroMetric);
      mPairCount = 0;
   }

   @Override
   public void apply(Pyroprint data_A, Pyroprint data_B) {
      Double comparison = mPyroComp.compare(mPyroMetric, data_A, data_B);

      int compareNdx = 0;

      for (; compareNdx < mPyroComparisons.size(); compareNdx++) {
         if (comparison.compareTo(mPyroComparisons.get(compareNdx)) < 0) {
            mPyroComparisons.add(compareNdx, comparison);
         }
      }

      if (compareNdx == mPyroComparisons.size()) {
         mPyroComparisons.add(comparison);
      }
   }

   @Override
   public Double result() {
      if (!mPyroComparisons.isEmpty()) {
         if (mPyroComparisons.size() % 2 != 0) {
            return mPyroComparisons.get(mPyroComparisons.size()/2);
         }
         else {
            return mPyroComparisons.get(mPyroComparisons.size()/2)/
             mPyroComparisons.get((mPyroComparisons.size()/2) + 1);
         }
      }

      else {
         Logger.getLogger().debug("ITS region has made no pairwise comparisons!");
         return new Double(0);
      }
   }
}
