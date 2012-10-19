package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;

import com.drin.java.metrics.Threshold;

import com.drin.java.metrics.DataMetric;
import com.drin.java.metrics.PyroprintComparator;
import com.drin.java.metrics.PyroprintMetric;

import com.drin.java.util.Logger;

/**
 * Calculates a distance metric for ITSRegions by comparing Pyroprints.
 * The pyroprints for the given regions are compared, which represents the
 * regions being compared. If the comparison between two pyroprints falls
 * below the beta threshold, then the comparison should go to 0.
 */
public abstract class ITSRegionMetric implements DataMetric<ITSRegion> {
   private static final double DEFAULT_ALPHA = 0.995,
                               DEFAULT_BETA = 0.99;

   protected double mResult;
   protected int mErrCode;

   protected Threshold mThreshold;
   protected ITSRegion mRegion;

   protected PyroprintComparator mPyroComp;
   protected PyroprintMetric mPyroMetric;

   public ITSRegionMetric(double alpha, double beta,
                          ITSRegion appliedRegion,
                          PyroprintComparator pyroComp,
                          PyroprintMetric pyroMetric) {

      mThreshold = new Threshold(alpha, beta);
      mRegion = appliedRegion;
      mPyroComp = pyroComp;
      mPyroMetric = pyroMetric;

      this.reset();
   }

   public ITSRegionMetric(ITSRegion appliedRegion,
                          PyroprintComparator pyroComp,
                          PyroprintMetric pyroMetric) {
      this(DEFAULT_ALPHA, DEFAULT_BETA, appliedRegion, pyroComp, pyroMetric);
   }

   @Override
   public void reset() {
      mResult = 0;
      mErrCode = 0;
   }

   @Override
   public abstract void apply(ITSRegion elem_A, ITSRegion elem_B);

   @Override
   public double result() {
      double result = mResult;

      Logger.error(mErrCode, "Error while computing ITSRegionMetric");

      reset();
      return result;
   }

   @Override
   public void setError(int errCode) { mErrCode = errCode; }

   @Override
   public int getError() { return mErrCode; }

   public ITSRegion getAppliedRegion() { return mRegion; }

   public double getAlphaThreshold() { return mThreshold.getAlpha(); }
   public double getBetaThreshold() { return mThreshold.getBeta(); }

   public double transformResult(double result) {
      if (result >= mThreshold.getAlpha()) { return 1; }
      else if (result < mThreshold.getBeta()) { return 0; }

      return result;
   }
}
