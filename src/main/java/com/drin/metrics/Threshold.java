package com.drin.metrics;

public final class Threshold {
   private final double mAlpha, mBeta;

   public Threshold(double alpha, double beta) {
      mAlpha = alpha;
      mBeta = beta;
   }

   public double getAlpha() { return mAlpha; }
   public double getBeta() { return mBeta; }
}
