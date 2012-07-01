package com.drin.java.types;

public final class Threshold {
   private final Double mAlphaThreshold, mBetaThreshold;

   public Threshold(double alpha, double beta) {
      mAlphaThreshold = alpha;
      mBetaThreshold = beta;
   }

   public Double getAlphaThreshold() {
      return mAlphaThreshold;
   }

   public Double getBetaThreshold() {
      return mBetaThreshold;
   }
}
