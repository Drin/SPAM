package com.drin.java.metrics;

public abstract class DataMetric<E> {
   protected float mResult;
   protected int mErrCode;

   public DataMetric() { this.reset(); }

   /**
    * Computes a comparison between elem_A and elem_B such that the result is
    * accumulated with the result associated with this DataMetric. This is
    * necessary for computing comparisons between multiple objects before
    * retrieving a final, aggregate result.
    */
   public abstract void apply(E elem_A, E elem_B);

   /**
    * Returns the computed result associated with this DataMetric. Used after
    * the metric has been applied.
    */
   public float result() {
      float result = mResult;

      this.reset();
      return result;
   }

   /**
    * Clears the computed value associated with this DataMetric. This ensures
    * that this DataMetric is ready for computation and prevents the need to
    * instantiate additional DataMetrics.
    */
   public void reset() { mResult = 0; }

   /**
    * Sets the error code associated with this DataMetric. This is a
    * convenience method, particularly for child classes.
    */
   public void setError(int errCode) { mErrCode = errCode; }

   /**
    * Clears the error code associated with this DataMetric. This ensures
    * that this DataMetric is ready for use and prevents the need to
    * instantiate additional DataMetrics.
    */
   public void resetError() { mErrCode = 0; }

   /**
    * Returns the error code associated with this DataMetric. If the error
    * code is 0 then no error occurred during execution. If the error code
    * is -1 then there was an error during computation of this metric.
    */
   public int getError() { return mErrCode; }
}
