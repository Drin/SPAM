package com.drin.java.metrics;

public interface DataMetric<E> {

   /**
    * Applies a particular metric defined by this class to the given comparison
    * value.
    */
   public void apply(E elem_A, E elem_B);

   /**
    * This gets the result of having applied this DataMetric to a series of
    * comparison values.
    */
   public double result();

   /**
    * This ensures that any state the metric builds up across calls to apply is
    * cleared.
    */
   public void reset();

   /**
    * A convenience method for storing an error code in case of an error during
    * metric computation.
    */
   public void setError(int errCode);


   /**
    * A getter for the error code that was set during an error in the
    * computation of this metric.
    */
   public int getError();
}
