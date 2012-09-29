package com.drin.java.metrics;

import com.drin.java.metrics.DataMetric;

public class TestMetric implements DataMetric<Double> {
   protected double mCount = 0;

   public void apply(Double dbl_A, Double dbl_B) {
      mCount += dbl_A.doubleValue() + dbl_B.doubleValue();
   }

   public Double result() {
      return mCount;
   }

   public void reset() {
      mCount = 0;
   }
}
