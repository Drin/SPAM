package com.drin.java.metrics;

import com.drin.java.types.Pyroprint;

public abstract class PyroprintMetric implements DataMetric<Double> {
   protected Double mResult;

   public PyroprintMetric() {
      mResult = null;
   }

   @Override
   public abstract void apply(Double elem_A, Double elem_B);

   @Override
   public abstract void reset();

   @Override
   public Double result() {
      return mResult;
   }
}
