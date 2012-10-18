package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;

public abstract class PyroprintMetric implements DataMetric<Pyroprint> {
   protected double mResult;
   protected int mErrCode;

   public PyroprintMetric() { this.reset(); }

   @Override
   public abstract void apply(Pyroprint elem_A, Pyroprint elem_B);

   @Override
   public abstract void reset();

   @Override
   public double result() { return mResult; }

   @Override
   public void setError(int errCode) { mErrCode = errCode; }

   @Override
   public int getError() { return mErrCode; }
}
