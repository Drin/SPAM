package com.drin.java.metrics;

import com.drin.java.biology.Pyroprint;
import com.drin.java.metrics.PyroprintMetric;

import java.util.List;

public class PyroprintComparator implements DataComparator<PyroprintMetric, Pyroprint> {
   @Override
   public Double compare(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      metric.apply(elem_A, elem_B);

      Double value = metric.result();

      if (System.getenv().containsKey("DEBUG")) {
         if (value != null) {
            System.err.printf("PyroprintComparator:\n\tCorrelation between " +
                              "'%s' and '%s': %.04f\n", elem_A.getName(),
                              elem_B.getName(), value);
         }
      }

      return value;
   }

   @Override
   public boolean isSimilar(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      throw new UnsupportedOperationException();
   }
}
