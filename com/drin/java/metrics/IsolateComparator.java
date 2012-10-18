package com.drin.java.metrics;

import com.drin.java.biology.Isolate;
import com.drin.java.metrics.IsolateMetric;

import com.drin.java.util.Logger;

import java.util.Map;

public class IsolateComparator implements DataComparator<IsolateMetric, Isolate> {
   @Override
   public double compare(IsolateMetric isoMetric, Isolate elem_A, Isolate elem_B) {
      isoMetric.apply(elem_A, elem_B);

      double comparison = isoMetric.result();

      Logger.debug(String.format("Isolate Comparator:\n\tisolate comparison: " +
                                 "%.04f", comparison));

      return comparison;
   }

   @Override
   public boolean isSimilar(IsolateMetric metric,
                            Isolate elem_A, Isolate elem_B) {
      throw new UnsupportedOperationException();
   }
}
