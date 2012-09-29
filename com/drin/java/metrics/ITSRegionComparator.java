package com.drin.java.metrics;

import com.drin.java.biology.ITSRegion;
import com.drin.java.metrics.ITSRegionMetric;

public class ITSRegionComparator implements DataComparator<ITSRegionMetric, ITSRegion> {

   @Override
   public Double compare(ITSRegionMetric regionMetric, ITSRegion elem_A, ITSRegion elem_B) {
      regionMetric.apply(elem_A, elem_B);

      Double comparison = regionMetric.result();

      if (System.getenv().containsKey("DEBUG")) {
         if (comparison != null) {
            System.err.printf("ITSRegionComparator:\n\tComparison between " +
                              "'%s' and '%s': %.04f\n", elem_A.getName(),
                              elem_B.getName(), comparison.doubleValue());
         }
      }

      return comparison;
   }

   @Override
   public boolean isSimilar(ITSRegionMetric regionMetric, ITSRegion elem_A, ITSRegion elem_B) {
      Double comparison = compare(regionMetric, elem_A, elem_B);

      return comparison != null && comparison.compareTo(regionMetric.getAlphaThreshold()) >= 0;
   }
}
