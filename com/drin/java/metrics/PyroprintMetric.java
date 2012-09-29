package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;

public abstract class PyroprintMetric implements DataMetric<Pyroprint> {
   protected Double mResult;

   public PyroprintMetric() { mResult = null; }

   @Override
   public abstract void apply(Pyroprint elem_A, Pyroprint elem_B);

   @Override
   public abstract void reset();

   @Override
   public Double result() {
      return mResult;
   }
}
