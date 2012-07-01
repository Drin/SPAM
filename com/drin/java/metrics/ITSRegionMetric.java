package com.drin.java.metrics;

import com.drin.java.types.ITSRegion;
import com.drin.java.types.Pyroprint;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.PyroprintMetric;

/**
 * Calculates a distance metric for ITSRegions by comparing Pyroprints.
 * The pyroprints for the given regions are compared, which represents the
 * regions being compared. If the comparison between two pyroprints falls
 * below the beta threshold, then the comparison should go to 0.
 */
public abstract class ITSRegionMetric implements DataMetric<Pyroprint> {
   private static final double DEFAULT_ALPHA = 0.997,
                               DEFAULT_BETA = 0.95;

   protected Double mAlpha, mBeta, mResult;

   protected PyroprintComparator mPyroComp;
   protected PyroprintMetric mPyroMetric;

   public ITSRegionMetric(double alphaThreshold, double betaThreshold,
    PyroprintComparator pyroComp, PyroprintMetric pyroMetric) {
      mPyroComp = pyroComp;
      mPyroMetric = pyroMetric;

      mAlpha = new Double(alphaThreshold);
      mBeta = new Double(betaThreshold);
      mResult = null;
   }

   public ITSRegionMetric(PyroprintComparator pyroComp, PyroprintMetric pyroMetric) {
      this(DEFAULT_ALPHA, DEFAULT_BETA, pyroComp, pyroMetric);
   }

   public abstract void reset();

   public double getAlphaThreshold() {
      return mAlpha.doubleValue();
   }

   public double getBetaThreshold() {
      return mBeta.doubleValue();
   }

   public double transformResult(double result) {
      if (result >= mAlpha.doubleValue()) {
         return 1;
      }

      else if (result < mBeta.doubleValue()) {
         return 0;
      }

      return result;
   }

   @Override
   public abstract void apply(Pyroprint data_A, Pyroprint data_B);

   @Override
   public Double result() { return mResult; }

}
