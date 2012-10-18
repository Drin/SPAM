package com.drin.java.metrics;

import com.drin.java.metrics.DataMetric;

public interface DataComparator<M extends DataMetric<E>, E> {

   /**
    * This method compares data objects data_A and data_B to get a distance
    * metric between the two.
    */
   public double compare(M metric, E elem_A, E elem_B);

   /**
    * This method compares object data_A and data_B to check if they are
    * similar (as opposed to dissimilar). This is useful for categorical
    * comparisons.
    */
   public boolean isSimilar(M metric, E elem_A, E elem_B);
}
