package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.PyroprintMetric;

import com.drin.java.util.Logger;

import java.util.List;

public class PyroprintComparator implements DataComparator<PyroprintMetric, Pyroprint> {
   @Override
   public double compare(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      metric.apply(elem_A, elem_B);

      double value = metric.result();

      Logger.error(metric.getError(),
                   String.format("PyroprintComparator:\n\tCorrelation " +
                                 "between '%s' and '%s': %.04f",
                                 elem_A.getName(), elem_B.getName(), value));

      return value;
   }

   @Override
   public boolean isSimilar(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      throw new UnsupportedOperationException();
   }
}
