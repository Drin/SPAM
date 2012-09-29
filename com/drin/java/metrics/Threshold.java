package com.drin.java.metrics;

public final class Threshold {
   private final double mAlphaThreshold, mBetaThreshold;

   public Threshold(double alpha, double beta) {
      mAlphaThreshold = alpha;
      mBetaThreshold = beta;
   }

   public double getAlphaThreshold() {
      return mAlphaThreshold;
   }

   public double getBetaThreshold() {
      return mBetaThreshold;
   }
}
