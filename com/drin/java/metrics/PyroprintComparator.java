package com.drin.java.metrics;

import com.drin.java.types.Pyroprint;
import com.drin.java.metrics.PyroprintMetric;

import java.util.List;

public class PyroprintComparator implements DataComparator<Pyroprint, PyroprintMetric> {
   private static final boolean DEBUG = false;

   @Override
   public Double compare(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      if (elem_A.getLength() == elem_B.getLength()) {
         List<Double> peakList_A = elem_A.getPeaks();
         List<Double> peakList_B = elem_B.getPeaks();

         if (DEBUG) { System.err.printf("Comparing pyroprints...\n"); }

         for (int ndx = 0; ndx < elem_A.getLength(); ndx++) {
            metric.apply(peakList_A.get(ndx), peakList_B.get(ndx));
         }
      }

      Double value = metric.result();
      metric.reset();

      if (DEBUG) {
         System.err.printf("PyroprintComparator:\n\tCorrelation between '%s' and '%s': %.04f\n",
          elem_A.getName(), elem_B.getName(), value);
      }

      return value;
   }

   @Override
   public boolean isSimilar(PyroprintMetric metric, Pyroprint elem_A, Pyroprint elem_B) {
      throw new UnsupportedOperationException();
   }
}
