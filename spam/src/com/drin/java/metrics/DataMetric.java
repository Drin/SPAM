package com.drin.java.metrics;

public abstract class DataMetric<E> {
   protected double mResult;
   protected int mErrCode;

   public DataMetric() { this.reset(); }

   /**
    * Applies a particular metric defined by this class to the given comparison
    * value.
    */
   public abstract void apply(E elem_A, E elem_B);

   /**
    * This gets the result of having applied this DataMetric to a series of
    * comparison values.
    */
   public double result() {
      double result = mResult;

      this.reset();
      return result;
   }

   /**
    * This ensures that any state the metric builds up across calls to apply is
    * cleared.
    */
   public void reset() { mResult = 0; }

   /**
    * A convenience method for storing an error code in case of an error during
    * metric computation.
    */
   public void setError(int errCode) { mErrCode = errCode; }

   /**
    * A getter for the error code that was set during an error in the
    * computation of this metric.
    */
   public int getError() { return mErrCode; }
}
