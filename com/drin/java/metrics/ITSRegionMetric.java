package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

/**
 * Calculates a distance metric for ITSRegions by comparing Pyroprints.
 * The pyroprints for the given regions are compared, which represents the
 * regions being compared. If the comparison between two pyroprints falls
 * below the beta threshold, then the comparison should go to 0.
 */
public abstract class ITSRegionMetric implements DataMetric<ITSRegion> {
   private static final double DEFAULT_ALPHA = 0.997,
                               DEFAULT_BETA = 0.95;

   protected double mAlpha, mBeta;
   protected Double mResult;
   protected PyroprintComparator mPyroComp;
   protected PyroprintMetric mPyroMetric;

   public ITSRegionMetric(double alphaThreshold, double betaThreshold,
                          PyroprintComparator pyroComp,
                          PyroprintMetric pyroMetric) {
      mPyroComp = pyroComp;
      mPyroMetric = pyroMetric;

      mAlpha = alphaThreshold;
      mBeta = betaThreshold;

      mResult = null;
   }

   public ITSRegionMetric(PyroprintComparator pyroComp, PyroprintMetric pyroMetric) {
      this(DEFAULT_ALPHA, DEFAULT_BETA, pyroComp, pyroMetric);
   }

   public abstract void reset();

   public double getAlphaThreshold() { return mAlpha; }
   public double getBetaThreshold() { return mBeta; }

   public double transformResult(double result) {
      if (result >= mAlpha) { return 1; }
      else if (result < mBeta) { return 0; }

      return result;
   }

   @Override
   public abstract void apply(ITSRegion data_A, ITSRegion data_B);

   @Override
   public Double result() {
      Double result = mResult;

      reset();
      return result;
   }

}
